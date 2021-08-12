package cn.bincker.web.blog.security.config;

import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.event.UserActionEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final ApplicationContext applicationContext;

    public CustomLogoutSuccessHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        var logoutEvent = new UserActionEvent(applicationContext, ((AuthorizationUser) authentication.getPrincipal()).getBaseUser(), UserActionEvent.ActionEnum.LOGOUT);
        applicationContext.publishEvent(logoutEvent);
    }
}
