package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.service.IArticleSubCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${system.base-path}/article/{articleId}/comment/{commentId}/comment")
public class ArticleSubCommentController {
    private final IArticleSubCommentService articleSubCommentService;

    public ArticleSubCommentController(IArticleSubCommentService articleSubCommentService) {
        this.articleSubCommentService = articleSubCommentService;
    }

    @PostMapping
    public ArticleCommentVo comment(@PathVariable Long commentId, @RequestBody @Validated ArticleCommentDto dto){
        return articleSubCommentService.comment(commentId, dto);
    }

    @DeleteMapping("{id}")
    public void del(@PathVariable Long id){
        articleSubCommentService.del(id);
    }

    @PostMapping("{id}/toggle-agree")
    public ValueVo<Boolean> toggleAgree(@PathVariable Long id){
        return articleSubCommentService.toggleAgree(id);
    }

    @PostMapping("{id}/toggle-tread")
    public ValueVo<Boolean> toggleTread(@PathVariable Long id){
        return articleSubCommentService.toggleTread(id);
    }

    @GetMapping
    public Page<ArticleCommentVo> getPage(@PathVariable Long commentId, @PageableDefault(sort = "createdDate") Pageable pageable){
        return articleSubCommentService.getPage(commentId, pageable);
    }
}
