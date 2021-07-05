package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ArticleComment extends Comment<Article> {
    @OneToMany(mappedBy = "target")
    private List<ArticleSubComment> subCommentList;
}
