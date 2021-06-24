package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.entity.ArticleCommentTread;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IArticleCommentTreadRepository extends CrudRepository<ArticleCommentTread, Long> {
    Optional<ArticleCommentTread> findByCreatedUserIdAndCommentId(Long uid, Long commentId);

    Long countByCommentId(Long id);

    List<ArticleCommentTread> findByCreatedUserIdAndCommentIdIn(Long uid, List<Long> commentIds);
}
