package cn.bincker.web.blog.base.event;

import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.text.DateFormat;
import java.util.Date;

public class UserActionEvent extends ApplicationEvent {
    @Getter
    private final Date createdDate;
    @Getter
    private final ActionEnum action;
    @Getter
    private final BaseEntity target;
    @Getter
    private final BaseUser user;
    @Getter
    private final Boolean logging;

    public UserActionEvent(Object source, BaseUser user, ActionEnum action, BaseEntity target, Boolean logging) {
        super(source);
        this.user = user;
        this.action = action;
        this.target = target;
        this.logging = logging;
        this.createdDate = new Date();
    }

    public UserActionEvent(Object source, BaseUser user, ActionEnum action, Boolean logging) {
        this(source, user, action, null, logging);
    }

    public UserActionEvent(Object source, BaseUser user, ActionEnum action, BaseEntity target) {
        this(source, user, action, target, true);
    }

    public UserActionEvent(Object source, BaseUser user, ActionEnum action) {
        this(source, user, action, null, true);
    }

    public String toLog(DateFormat dateFormat){
        return "user " + this.getUser().getUsername() + "(id=" + this.getUser().getId() + ") did " +
                        this.getAction() + "(targetId=" + (this.getTarget() == null ? null : this.getTarget().getId()) + ") at " +
                        dateFormat.format(this.getCreatedDate());
    }

    public enum ActionEnum{
        LOGIN,
        LOGOUT,
    }
}
