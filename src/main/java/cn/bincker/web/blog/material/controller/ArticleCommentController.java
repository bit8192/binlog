package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.service.IArticleCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentListVo;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${system.base-path}/article/{articleId}/comment")
public class ArticleCommentController {
    private final IArticleCommentService articleCommentService;

    public ArticleCommentController(IArticleCommentService articleCommentService) {
        this.articleCommentService = articleCommentService;
    }

    @PostMapping
    public ArticleCommentVo comment(@PathVariable Long articleId, @RequestBody @Validated ArticleCommentDto dto){
        return articleCommentService.comment(articleId, dto);
    }

    @DeleteMapping("{commentId}")
    public void del(@PathVariable Long commentId){
        articleCommentService.del(commentId);
    }

    @PostMapping("{id}/toggle-agree")
    public ValueVo<Boolean> toggleAgree(@PathVariable Long id){
        return articleCommentService.toggleAgree(id);
    }

    @PostMapping("{id}/toggle-tread")
    public ValueVo<Boolean> toggleTread(@PathVariable Long id){
        return articleCommentService.toggleTread(id);
    }

    @GetMapping
    public Page<ArticleCommentListVo> getPage(@PathVariable Long articleId, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return articleCommentService.getPage(articleId, pageable);
    }
}
