package cn.bincker.web.blog.security.config;

import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.event.UserActionEvent;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.vo.SuccessMsgVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private final IBaseUserService userService;

    public AuthenticationHandler(ObjectMapper objectMapper, ApplicationContext applicationContext, IBaseUserService userService) {
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), new SuccessMsgVo(false, "认证失败"));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        httpServletResponse.setStatus(HttpStatus.OK.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(httpServletResponse.getWriter(), new SuccessMsgVo("认证成功"));

        var authorizationUser = (AuthorizationUser) authentication.getPrincipal();
        var userActionEvent = new UserActionEvent(applicationContext, authorizationUser.getBaseUser(), UserActionEvent.ActionEnum.LOGIN_PASSWORD);
        applicationContext.publishEvent(userActionEvent);

//        第三方帐号绑定
        var session = httpServletRequest.getSession();
//        QQ
        var qqOpenId = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_QQ_OPENID);
        if (StringUtils.hasText(qqOpenId)) {
            userService.bindQqOpenId(authorizationUser.getBaseUser(), qqOpenId);
        }
//        Github
        var github = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_GITHUB);
        if (StringUtils.hasText(github)) {
            userService.bindGithub(authorizationUser.getBaseUser(), github);
        }
    }
}
