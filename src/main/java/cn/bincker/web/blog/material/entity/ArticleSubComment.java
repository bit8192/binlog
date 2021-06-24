package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 子评论
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class ArticleSubComment extends Comment<ArticleComment> {
    private Boolean recommend;
}
