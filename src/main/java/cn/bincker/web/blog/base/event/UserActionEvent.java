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

    public UserActionEvent(Object source, BaseUser user, ActionEnum action, BaseEntity target) {
        super(source);
        this.user = user;
        this.action = action;
        this.target = target;
        this.createdDate = new Date();
    }

    public UserActionEvent(Object source, BaseUser user, ActionEnum action) {
        this(source, user, action, null);
    }

    public enum ActionEnum{
        LOGIN_PASSWORD,
        LOGIN_GITHUB,
        LOGIN_QQ,
        LOGIN_WECHAT,
        LOGIN_PHONE,
        LOGOUT,
    }
}
