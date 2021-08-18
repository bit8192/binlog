package cn.bincker.web.blog.base.event;

import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.text.DateFormat;
import java.util.Date;

@Getter
public class UserActionEvent extends ApplicationEvent {
    private final Date createdDate;
    private final ActionEnum action;
    private final BaseEntity target;
    private final BaseUser user;
    private final String additionalInfo;

    public UserActionEvent(Object source, BaseUser user, ActionEnum action, BaseEntity target, String additionalInfo) {
        super(source);
        this.user = user;
        this.action = action;
        this.target = target;
        this.createdDate = new Date();
        this.additionalInfo = additionalInfo;
    }

    public UserActionEvent(Object source, BaseUser user, ActionEnum action, BaseEntity target) {
        this(source, user, action, target, "");
    }

    public UserActionEvent(Object source, BaseUser user, ActionEnum action) {
        this(source, user, action, null);
    }

    public enum ActionEnum{
//        帐号密码登录
        LOGIN_PASSWORD,
//        第三方登录
        LOGIN_OAUTH2,
//        注册后的自动登录
        LOGIN_REGISTER,
//        绑定帐号
        BIND_ACCOUNT_OAUTH2,
        LOGOUT,
        REGISTER,
    }
}
