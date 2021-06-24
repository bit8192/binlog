package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import cn.bincker.web.blog.material.entity.ArticleSubCommentAgree;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IArticleSubCommentAgreeRepository extends CrudRepository<ArticleSubCommentAgree, Long> {
    Optional<ArticleSubCommentAgree> findByCreatedUserAndComment(BaseUser user, ArticleSubComment comment);

    Long countByComment(ArticleSubComment comment);

    List<ArticleSubCommentAgree> findByCreatedUserIdAndCommentIdIn(Long uid, List<Long> commentIds);
}
