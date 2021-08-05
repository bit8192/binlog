package cn.bincker.web.blog.base.listener;

import cn.bincker.web.blog.base.event.LoginEvent;
import cn.bincker.web.blog.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class LoginEventListener implements ApplicationListener<LoginEvent> {
    private static final Logger log = LoggerFactory.getLogger(LoginEventListener.class);

    private final DateUtils dateUtils;

    public LoginEventListener(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    @Override
    public void onApplicationEvent(LoginEvent loginEvent) {
        var user = loginEvent.getUser();
        log.info("user " + user.getUsername() + "(id=" + user.getId() + ") login at " + dateUtils.getDatetimeFormat().format(loginEvent.getCreatedDate()));
    }
}
