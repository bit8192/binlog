package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.service.IQQAuthorizeService;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;

@ConditionalOnBean(IQQAuthorizeService.class)
@Controller
@RequestMapping("${binlog.base-path}/authorize/qq")
public class QQAuthorizeController{
    private static final String CACHE_KEY_QQ_AUTHORIZE_STATE = "QQ-AUTHORIZE-STATE-";
    private static final long STATE_ALIVE_TIMEOUT = 10 * 60 * 1000L;
    private final String basePath;
    private final IQQAuthorizeService qqAuthorizeService;
    private final IBaseUserService userService;
    private final ISystemCacheService systemCacheService;

    public QQAuthorizeController(@Value("${binlog.base-path}") String basePath, IQQAuthorizeService qqAuthorizeService, IBaseUserService userService, ISystemCacheService systemCacheService) {
        this.basePath = basePath;
        this.qqAuthorizeService = qqAuthorizeService;
        this.userService = userService;
        this.systemCacheService = systemCacheService;
    }

    /**
     * 前台请求登录，然后跳转到qq授权页面
     */
    @GetMapping("login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var state = Long.toHexString(System.nanoTime());
        while (systemCacheService.containsKey(state)) state = Long.toHexString(System.nanoTime());//不能有重复的
        systemCacheService.put(CACHE_KEY_QQ_AUTHORIZE_STATE + state, new BaseUser(), Duration.ofMinutes(STATE_ALIVE_TIMEOUT));
        request.getSession().setAttribute(SessionKeyConstant.SESSION_KEY_QQ_AUTHORIZE_STATE, state);
        String redirectUrl = RequestUtils.getRequestBaseUrl(request) + basePath + "/authorize/qq/notice";
        response.sendRedirect(qqAuthorizeService.getAuthorizeUrl(redirectUrl, state));
    }

    /**
     * 授权成功，qq回调传递code
     */
    @GetMapping("notice")
    public void notice(String code, String state){
        if(!systemCacheService.containsKey(state)) throw new BadRequestException();
        var accessToken = qqAuthorizeService.getAccessToken(code, state);
        var openId = qqAuthorizeService.getOpenId(accessToken.getAccessToken());
        var userOptional = userService.findByQQOpenId(openId);
        if(userOptional.isEmpty()){
            var userInfo = qqAuthorizeService.getUserInfo(accessToken.getAccessToken(), openId);
            var userName = userInfo.getNickname().replaceAll(RegexpConstant.ILLEGAL_USERNAME_VALUE, "");
            var tempUsername = userName;
            var suffixIndex = 1;
            while (userService.findByUsername(tempUsername).isPresent()) tempUsername += suffixIndex ++;//加后缀，直至没有用过这个用户名
            userName = tempUsername;
            var user = new BaseUser();
            user.setUsername(userName);
            if(StringUtils.hasText(userInfo.getFigureurl_qq_2())){
                user.setHeadImg(userInfo.getFigureurl_qq_2());
            }else{
                user.setHeadImg(userInfo.getFigureurl_qq_1());
            }
            systemCacheService.put(CACHE_KEY_QQ_AUTHORIZE_STATE + state, user, Duration.ofMinutes(STATE_ALIVE_TIMEOUT));
        }else{
            systemCacheService.put(CACHE_KEY_QQ_AUTHORIZE_STATE + state, userOptional.get(), Duration.ofMinutes(STATE_ALIVE_TIMEOUT));
        }
    }

    @GetMapping("check-notice")
    public ValueVo<Object> checkNotice(HttpSession session){
        var state = (String) session.getAttribute(SessionKeyConstant.SESSION_KEY_QQ_AUTHORIZE_STATE);
        if(state == null) return new ValueVo<>(false);
        var userOptional = systemCacheService.getValue(CACHE_KEY_QQ_AUTHORIZE_STATE + state, BaseUser.class);
        if(userOptional.isEmpty()) return new ValueVo<>(false);
        if(userOptional.get().getId() != null){//如果没有id就表示没有注册，注册前需要改用户名
            var securityContext = SecurityContextHolder.getContext();
            var user = userOptional.get();
            var authentication = new UsernamePasswordAuthenticationToken(new AuthorizationUser(user), user.getEncodedPasswd(), user.getRoles());
            securityContext.setAuthentication(authentication);
        }
        return new ValueVo<>(userOptional.get());
    }
}
