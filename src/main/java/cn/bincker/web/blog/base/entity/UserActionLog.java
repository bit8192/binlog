package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.event.UserActionEvent;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class UserActionLog extends BaseEntity{
    @Enumerated(EnumType.STRING)
    private UserActionEvent.ActionEnum action;

    private Long targetId;

    @ManyToOne
    private BaseUser user;

    private String additionalInfo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserActionLog that = (UserActionLog) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return 52259602;
    }
}
