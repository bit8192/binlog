package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@EntityListeners(UserAuditingListener.class)
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
public class ArticleAgree extends BaseEntity {
    @ManyToOne
    private BaseUser createdUser;

    @ManyToOne
    private Article article;
}
