package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.material.entity.ArticleAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IArticleAgreeRepository extends JpaRepository<ArticleAgree, Long> {
    Optional<ArticleAgree> findByArticleIdAndCreatedUserId(Long articleId, Long createdUserId);

    List<ArticleAgree> findAllByCreatedUserIdAndArticleIdIn(Long id, List<Long> articleIds);

    Long countByArticleId(Long articleId);

    Long countByArticleCreatedUser(BaseUser user);
}
