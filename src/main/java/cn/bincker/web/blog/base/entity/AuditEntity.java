package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@EntityListeners(UserAuditingListener.class)
@Getter
@Setter
public class AuditEntity extends BaseEntity{
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JsonIgnore
    private BaseUser createdUser;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JsonIgnore
    private BaseUser lastModifiedUser;
}
