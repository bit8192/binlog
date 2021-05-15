package cn.bincker.web.blog.material.service;

import cn.bincker.web.blog.material.service.dto.ArticleClassPostDto;
import cn.bincker.web.blog.material.service.dto.ArticleClassPutDto;
import cn.bincker.web.blog.material.service.vo.ArticleClassVo;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface IArticleClassService {
    List<ArticleClassVo> findAllByParentId(@Nullable Long id);

    ArticleClassVo add(ArticleClassPostDto articleClass);

    Optional<ArticleClassVo> getById(Long id);

    void delete(Long id);

    boolean exists(Long id);

    ArticleClassVo save(ArticleClassPutDto articleClassPutDto);
}
