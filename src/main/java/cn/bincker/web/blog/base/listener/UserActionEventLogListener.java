package cn.bincker.web.blog.base.listener;

import cn.bincker.web.blog.base.entity.UserActionLog;
import cn.bincker.web.blog.base.event.UserActionEvent;
import cn.bincker.web.blog.base.repository.IUserActionLogRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UserActionEventLogListener implements ApplicationListener<UserActionEvent> {
    private final IUserActionLogRepository userActionLogRepository;

    public UserActionEventLogListener(IUserActionLogRepository userActionLogRepository) {
        this.userActionLogRepository = userActionLogRepository;
    }

    @Override
    public void onApplicationEvent(UserActionEvent event) {
        var log = new UserActionLog();
        log.setAction(event.getAction());
        log.setUser(event.getUser());
        log.setCreatedDate(event.getCreatedDate());
        log.setTargetId(event.getTarget() == null ? null : event.getTarget().getId());
        log.setAdditionalInfo(event.getAdditionalInfo());
        userActionLogRepository.save(log);
    }
}
