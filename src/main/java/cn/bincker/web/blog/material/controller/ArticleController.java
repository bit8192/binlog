package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.dto.valid.InsertValid;
import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.service.ICommentService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.CommentVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleDto;
import cn.bincker.web.blog.material.service.IArticleService;
import cn.bincker.web.blog.material.vo.ArticleListVo;
import cn.bincker.web.blog.material.vo.ArticleVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;

@RequestMapping("/article")
@RestController
public class ArticleController {
    private final IArticleService articleService;

    public ArticleController(IArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("{id}")
    public ArticleVo getDetail(@PathVariable Long id){
        return articleService.getDetail(id);
    }

    /**
     * 点击和取消赞
     */
    @PostMapping("{id}/toggle-agree")
    public ValueVo<Boolean> toggleAgree(@PathVariable Long id){
        return articleService.toggleAgreed(id);
    }

    @GetMapping
    public Page<ArticleListVo> getPage(String keywords, Long articleClassId, Long[] tagIds, Pageable pageable){
        return articleService.pageAll(keywords, articleClassId, tagIds, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleVo add(@Validated(InsertValid.class) @RequestBody ArticleDto dto){
        return articleService.insert(dto);
    }

    @PostMapping("{id}/view")
    public void view(@PathVariable Long id){
        articleService.view(id);
    }

    @PutMapping
    public ArticleVo update(@Validated(UpdateValid.class) @RequestBody ArticleDto dto){
        return articleService.update(dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        articleService.delete(id);
    }

    @GetMapping("{articleId}/comments")
    public Page<CommentVo> getPage(@PathVariable Long articleId, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return articleService.getCommentPage(articleId, pageable);
    }

    @PostMapping("{articleId}/comments")
    public CommentVo commenting(@PathVariable Long articleId, @RequestBody @Validated CommentDto dto){
        return articleService.commenting(articleId, dto);
    }
}
