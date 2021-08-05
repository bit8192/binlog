package cn.bincker.web.blog.base.event;

import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

public class LogoutEvent extends ApplicationEvent {
    @Getter
    private final BaseUser user;
    @Getter
    private final Date createdDate;

    public LogoutEvent(Object source, BaseUser user, Date createdDate) {
        super(source);
        this.user = user;
        this.createdDate = createdDate;
    }
}
