package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IArticleSubCommentRepository extends JpaRepository<ArticleSubComment, Long> {
    List<ArticleSubComment> findAllByRecommendIsTrueAndTargetIn(List<ArticleComment> articleComments);

    Page<ArticleSubComment> findAllByTarget(ArticleComment articleComment, Pageable pageable);

    Long countByTarget(ArticleComment articleComment);
}
