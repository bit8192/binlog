package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners(UserAuditingListener.class)
@Getter
public class AuditEntity extends BaseEntity{
    @CreatedBy
    @ManyToOne
    private BaseUser createdUser;

    @LastModifiedBy
    @ManyToOne
    private BaseUser lastModifiedUser;
}
