package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IArticleRepository extends JpaRepository<Article, Long> {
}
