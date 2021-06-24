package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import cn.bincker.web.blog.material.entity.ArticleSubCommentTread;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IArticleSubCommentTreadRepository extends CrudRepository<ArticleSubCommentTread, Long> {
    Optional<ArticleSubCommentTread> findByCreatedUserAndComment(BaseUser user, ArticleSubComment comment);

    Long countByComment(ArticleSubComment comment);

    List<ArticleSubCommentTread> findByCreatedUserIdAndCommentIdIn(Long uid, List<Long> commentIds);
}
