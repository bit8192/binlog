package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@EntityListeners(UserAuditingListener.class)
@Data
public class AuditEntity extends BaseEntity{
    @CreatedBy
    @ManyToOne
    @NotNull
    private BaseUser createdUser;

    @LastModifiedBy
    @ManyToOne
    @NotNull
    private BaseUser lastModifiedUser;
}
