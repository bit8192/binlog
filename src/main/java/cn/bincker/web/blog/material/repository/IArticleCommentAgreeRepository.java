package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.material.entity.ArticleCommentAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IArticleCommentAgreeRepository extends JpaRepository<ArticleCommentAgree, Long> {
    Optional<ArticleCommentAgree> findByCreatedUserIdAndCommentId(Long uid, Long commentId);

    Long countByCommentId(Long commentId);

    List<ArticleCommentAgree> findByCreatedUserIdAndCommentIdIn(Long uid, List<Long> commentIds);

    Long countByCommentCreatedUser(BaseUser user);
}
