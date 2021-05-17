package cn.bincker.web.blog.material.repository;

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
    Page<Article> findAll(Pageable pageable);

    @Query("from Article a where a.title like %?1% or a.content like %?1%")
    @EntityGraph("Article.default")
    Page<Article> findAllByKeyword(String keyword, Pageable pageable);

    @EntityGraph("Article.default")
    Page<Article> findAllByArticleClassId(Long articleClassId, Pageable pageable);

    @EntityGraph("Article.default")
    Page<Article> findAllByTagsId(Long tagId, Pageable pageable);
}
