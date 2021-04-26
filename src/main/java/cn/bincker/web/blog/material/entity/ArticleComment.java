package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ArticleComment extends AuditEntity {
    private String content;

    @ManyToOne
    private ArticleComment parent;

    @ManyToOne
    private Article article;

    private long agreedNum;

    private long treadNum;
}
