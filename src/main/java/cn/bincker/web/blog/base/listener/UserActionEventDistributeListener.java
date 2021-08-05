package cn.bincker.web.blog.base.listener;

import cn.bincker.web.blog.base.event.LoginEvent;
import cn.bincker.web.blog.base.event.LogoutEvent;
import cn.bincker.web.blog.base.event.UserActionEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UserActionEventDistributeListener implements ApplicationListener<UserActionEvent> {
    private final ApplicationContext applicationContext;

    public UserActionEventDistributeListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(UserActionEvent userActionEvent) {
        switch (userActionEvent.getAction()){
            case LOGIN -> applicationContext.publishEvent(new LoginEvent(applicationContext, userActionEvent.getUser(), userActionEvent.getCreatedDate()));
            case LOGOUT -> applicationContext.publishEvent(new LogoutEvent(applicationContext, userActionEvent.getUser(), userActionEvent.getCreatedDate()));
        }
    }
}
