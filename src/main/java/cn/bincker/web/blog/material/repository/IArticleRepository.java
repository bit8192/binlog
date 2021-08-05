package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Comment;
import cn.bincker.web.blog.material.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IArticleRepository extends JpaRepository<Article, Long> {
    @Override
    @EntityGraph("Article.default")
    Optional<Article> findById(Long aLong);

    @Override
    @EntityGraph("Article.default")
    @Query("from Article a where a.isPublic = true")
    Page<Article> findAll(Pageable pageable);

    @Query("from Article a where a.isPublic = true or a.createdUser.id = ?1")
    @EntityGraph("Article.default")
    Page<Article> findAllWithUserId(Long userId, Pageable pageable);

    @Query("from Article a where a.isPublic = true and ( a.title like %?1% or a.content like %?1% )")
    @EntityGraph("Article.default")
    Page<Article> findAllByKeywords(String keyword, Pageable pageable);

    @Query("from Article a where ( a.isPublic = true or a.createdUser.id = ?1 ) and ( a.title like %?2% or a.content like %?2% )")
    @EntityGraph("Article.default")
    Page<Article> findAllByKeywordsWithUserId(Long userId, String keyword, Pageable pageable);

    @EntityGraph("Article.default")
    Page<Article> findAllByArticleClassIdAndIsPublicTrue(Long articleClassId, Pageable pageable);

    @Query("from Article a where ( a.isPublic = true or a.createdUser.id = ?1 ) and a.articleClass.id = ?2")
    @EntityGraph("Article.default")
    Page<Article> findAllByArticleClassIdWithUserId(Long userId, Long articleClassId, Pageable pageable);

    @EntityGraph("Article.default")
    Page<Article> findAllByTagsIdAndIsPublicTrue(Long tagId, Pageable pageable);

    @Query("from Article a join a.tags tag where ( a.isPublic = true or a.createdUser.id = ?1 ) and tag.id = ?2")
    @EntityGraph("Article.default")
    Page<Article> findAllByTagsIdWithUserId(Long userId, Long tagId, Pageable pageable);

    Long countByCreatedUser(BaseUser user);

    @Query("select c from Article a join a.comments c where a.id = :articleId")
    Page<Comment> selectCommentPage(Long articleId, Pageable pageable);

    @Query("from Article a join a.comments c where c.id = :commentId")
    Optional<Article> findByCommentId(Long commentId);
}
