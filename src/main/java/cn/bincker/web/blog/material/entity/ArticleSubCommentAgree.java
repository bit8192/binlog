package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ArticleSubCommentAgree extends BaseEntity {
    @ManyToOne
    private BaseUser createdUser;

    @ManyToOne
    private ArticleSubComment comment;
}
