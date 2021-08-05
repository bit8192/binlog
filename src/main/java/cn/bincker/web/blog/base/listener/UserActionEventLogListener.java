package cn.bincker.web.blog.base.listener;

import cn.bincker.web.blog.base.event.UserActionEvent;
import cn.bincker.web.blog.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UserActionEventLogListener implements ApplicationListener<UserActionEvent> {
    private static final Logger log = LoggerFactory.getLogger(UserActionEventLogListener.class);
    private final DateUtils dateUtils;

    public UserActionEventLogListener(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    @Override
    public void onApplicationEvent(UserActionEvent userActionEvent) {
        if(!userActionEvent.getLogging()) return;
        log.info(userActionEvent.toLog(dateUtils.getDatetimeFormat()));
    }
}
