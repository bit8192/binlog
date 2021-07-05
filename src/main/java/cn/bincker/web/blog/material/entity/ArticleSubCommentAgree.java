package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;

@EntityListeners(UserAuditingListener.class)
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ArticleSubCommentAgree extends BaseEntity {
    @CreatedBy
    @ManyToOne
    private BaseUser createdUser;

    @ManyToOne
    private ArticleSubComment comment;
}
