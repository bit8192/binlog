package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.service.IQQAuthorizeService;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.beans.factory.DisposableBean;
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
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ConditionalOnBean(IQQAuthorizeService.class)
@Controller
@RequestMapping("${system.base-path}/authorize/qq")
public class QQAuthorizeController implements DisposableBean {
    private final String basePath;
    private final Map<String, Optional<BaseUser>> authorizationMap;//用于存储授权信息和判断是否存在
    private final PriorityQueue<StateInfo> stateInfoPriorityQueue;//存储state和其创建时间信息，定时删除
    @SuppressWarnings("FieldCanBeLocal")
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final IQQAuthorizeService qqAuthorizeService;
    private final IBaseUserService userService;

    public QQAuthorizeController(@Value("${system.base-path}") String basePath, IQQAuthorizeService qqAuthorizeService, IBaseUserService userService) {
        this.basePath = basePath;
        this.qqAuthorizeService = qqAuthorizeService;
        this.userService = userService;
        authorizationMap = new HashMap<>();
        stateInfoPriorityQueue = new PriorityQueue<>((a, b)-> (int) (a.time - b.time));
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.schedule(getClearStateQueueRunnable(), 30, TimeUnit.MINUTES);
    }

    /**
     * 定时清除state
     */
    private Runnable getClearStateQueueRunnable(){
        return ()->{
            var currentTimeMillis = System.currentTimeMillis();
            StateInfo state;
            synchronized (authorizationMap) {
                while ((state = stateInfoPriorityQueue.peek()) != null) {
                    if (currentTimeMillis - state.time > 600000) {//超过十分钟则删除
                        stateInfoPriorityQueue.poll();
                        authorizationMap.remove(state.state);
                    } else {//否则跳过，不再进行检查，因为后面的时间都更大
                        break;
                    }
                }
            }
        };
    }

    /**
     * 前台请求登录，然后跳转到qq授权页面
     */
    @GetMapping("login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var state = new StateInfo();
        while (authorizationMap.containsKey(state.state)) state = new StateInfo();//不能有重复的
        synchronized (authorizationMap){
            stateInfoPriorityQueue.add(state);
            authorizationMap.put(state.state, Optional.empty());
        }
        request.getSession().setAttribute(SessionKeyConstant.SESSION_KEY_QQ_AUTHORIZE_STATE, state.state);
        String redirectUrl = buildOriginUrl(request.getScheme(), request.getServerName(), request.getServerPort(), basePath + "/authorize/qq/notice");
        response.sendRedirect(qqAuthorizeService.getAuthorizeUrl(redirectUrl, state.state));
    }

    /**
     * 授权成功，qq回调传递code
     */
    @GetMapping("notice")
    public void notice(String code, String state){
        if(!authorizationMap.containsKey(state)) throw new BadRequestException();
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
            synchronized (authorizationMap){
                authorizationMap.put(state, Optional.of(user));
            }
        }else{
            synchronized (authorizationMap){
                authorizationMap.put(state, userOptional);
            }
        }
    }

    @GetMapping("check-notice")
    public ValueVo<Object> checkNotice(HttpSession session){
        var state = (String) session.getAttribute(SessionKeyConstant.SESSION_KEY_QQ_AUTHORIZE_STATE);
        if(state == null) return new ValueVo<>(false);
        var userOptional = authorizationMap.get(state);
        if(userOptional.isEmpty()) return new ValueVo<>(false);
        if(userOptional.get().getId() != null){//如果没有id就表示没有注册，注册前需要改用户名
            var securityContext = SecurityContextHolder.getContext();
            var user = userOptional.get();
            var authentication = new UsernamePasswordAuthenticationToken(new AuthorizationUser(user), user.getEncodedPasswd(), user.getRoles());
            securityContext.setAuthentication(authentication);
        }
        return new ValueVo<>(userOptional.get());
    }

    private String buildOriginUrl(String scheme, String serverName, int port, String path){
        if(port == 80 || port == 443){
            return scheme + "://" + serverName + path;
        }
        return scheme + "://" + serverName + ":" + port + path;
    }

    /**
     * bean被销毁时关闭state清除线程池
     */
    @Override
    public void destroy() {
        scheduledThreadPoolExecutor.shutdown();
    }

    private static class StateInfo{
        private final String state;
        private final long time;

        public StateInfo() {
            state = Long.toHexString(System.nanoTime());
            time = System.currentTimeMillis();
        }
    }
}
