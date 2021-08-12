package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.event.UserActionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class UserActionLog extends BaseEntity{
    @Enumerated(EnumType.STRING)
    private UserActionEvent.ActionEnum action;

    private Long targetId;

    @ManyToOne
    private BaseUser user;
}
