package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@EntityListeners(UserAuditingListener.class)
public class CommentAgree extends BaseEntity{
    @NotNull
    @ManyToOne
    private Comment comment;

    @ManyToOne
    @NotNull
    @CreatedBy
    private BaseUser createdUser;
}
