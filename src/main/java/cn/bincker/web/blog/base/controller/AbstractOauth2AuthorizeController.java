package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.entity.Oauth2AccessToken;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Oauth2UserInfo;
import cn.bincker.web.blog.base.event.UserActionEvent;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.service.IOauth2AuthorizeService;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.SuccessMsgVo;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

public abstract class AbstractOauth2AuthorizeController<T extends Oauth2AccessToken, U extends Oauth2UserInfo> {
    private static final String CACHE_KEY_AUTHORIZE_STATE = "AUTHORIZE-STATE-";
    private static final long STATE_ALIVE_TIMEOUT = 10 * 60 * 1000L;

    protected final IBaseUserService userService;
    private final ISystemCacheService systemCacheService;
    private final IOauth2AuthorizeService<T,U> oauth2AuthorizeService;

    protected AbstractOauth2AuthorizeController(IBaseUserService userService, ISystemCacheService systemCacheService, IOauth2AuthorizeService<T, U> oauth2AuthorizeService) {
        this.userService = userService;
        this.systemCacheService = systemCacheService;
        this.oauth2AuthorizeService = oauth2AuthorizeService;
    }

    /**
     * 前台请求登录，然后跳转到qq授权页面
     */
    @GetMapping("login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var state = Long.toHexString(System.nanoTime());
        synchronized (systemCacheService) {
            while (systemCacheService.containsKey(state)) state = Long.toHexString(System.nanoTime());//不能有重复的
            systemCacheService.put(CACHE_KEY_AUTHORIZE_STATE + state, null, STATE_ALIVE_TIMEOUT);
        }
        request.getSession().setAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_STATE, state);
        var redirectUrl = getRedirectUrl(request);
        response.sendRedirect(oauth2AuthorizeService.getAuthorizeUrl(redirectUrl, state));
    }

    /**
     * 授权成功，qq回调传递code
     */
    @GetMapping("notice")
    public void notice(HttpServletRequest request, String code, String state){
        if(!systemCacheService.containsKey(CACHE_KEY_AUTHORIZE_STATE + state)) throw new BadRequestException();
        var accessToken = oauth2AuthorizeService.getAccessToken(request, code);
        var userInfo = oauth2AuthorizeService.getUserInfo(accessToken.getAccessToken());
        var userOptional = findUserByUserInfo(userInfo);
        if(userOptional.isEmpty()){
            var userName = userInfo.getUsername().replaceAll(RegexpConstant.ILLEGAL_USERNAME_VALUE, "");
            var tempUsername = userName;
            var suffixIndex = 1;
            while (userService.findByUsername(tempUsername).isPresent()) tempUsername += suffixIndex ++;//加后缀，直至没有用过这个用户名
            userName = tempUsername;
            var user = new BaseUser();
            setUserInfo(user, userInfo);
            user.setUsername(userName);
            user.setHeadImg(userInfo.getHeadImg());
            systemCacheService.put(CACHE_KEY_AUTHORIZE_STATE + state, user, STATE_ALIVE_TIMEOUT);
        }else{
            systemCacheService.put(CACHE_KEY_AUTHORIZE_STATE + state, userOptional.get(), STATE_ALIVE_TIMEOUT);
        }
    }

    /**
     * 前端检测是否登录成功
     */
    @GetMapping("check-notice")
    @ResponseBody
    public Object checkNotice(HttpSession session, BaseUser currentUser){
        var state = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_STATE);
        if(state == null) return new SuccessMsgVo(false);
        var userOptional = systemCacheService.getValue(CACHE_KEY_AUTHORIZE_STATE + state, BaseUser.class);
        if(userOptional.isEmpty()) return new SuccessMsgVo(false);

        var user = userOptional.get();
        if(user.getId() != null) {//已经注册了的进行登录, 没注册的前端判断然后进行注册
//            若已经登录，要么进行帐号合并，要么提示用户然后不处理
            if(currentUser != null){
                return new SuccessMsgVo(false, "该帐号已注册，帐号合并功能未实现");
            }
            var msg = "登录成功";
//            如果授权过其他第三方帐号，那么进行绑定
            if(!this.getClass().equals(QQAuthorizeController.class)) {
                var qqOpenId = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_QQ_OPENID);
                if (StringUtils.hasText(qqOpenId)) {
                    user.setQqOpenId(qqOpenId);
                    userService.bindQqOpenId(user, qqOpenId);
                    msg = "绑定QQ成功";
                }
            }
            if(!this.getClass().equals(GithubAuthorizeController.class)) {
                var github = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_GITHUB);
                if (StringUtils.hasText(github)) {
                    user.setGithub(github);
                    userService.bindGithub(user, github);
                    msg = "绑定Github成功";
                }
            }
//            登录
            userService.login(user, UserActionEvent.ActionEnum.LOGIN_OAUTH2, getName());
            systemCacheService.remove(CACHE_KEY_AUTHORIZE_STATE + state);
            session.removeAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_STATE);
            return new SuccessMsgVo(true, msg);
        }else if(currentUser != null){
            var msg = "绑定失败";
            var github = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_GITHUB);
            if (StringUtils.hasText(github)) {
                user.setGithub(github);
                userService.bindGithub(currentUser, github);
                msg = "绑定Github成功";
            }
            var qqOpenId = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_QQ_OPENID);
            if (StringUtils.hasText(qqOpenId)) {
                user.setQqOpenId(qqOpenId);
                userService.bindQqOpenId(currentUser, qqOpenId);
                msg = "绑定QQ成功";
            }
            return new SuccessMsgVo(msg);
        }

        var vo = new BaseUserVo(user);
        vo.setQqOpenId(user.getQqOpenId());
        vo.setWechatOpenId(user.getWechatOpenId());
        if(user.getId() == null) configSessionBeforeRegister(session, vo);
        return vo;
    }

    abstract String getRedirectUrl(HttpServletRequest request);

    abstract Optional<BaseUser> findUserByUserInfo(U userInfo);

    abstract void setUserInfo(BaseUser user, U oauth2UserInfo);

    abstract String getName();

    abstract void configSessionBeforeRegister(HttpSession session, BaseUserVo vo);
}
