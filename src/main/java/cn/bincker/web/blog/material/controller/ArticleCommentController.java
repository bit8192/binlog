package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.service.IArticleCommentService;
import cn.bincker.web.blog.material.service.IArticleSubCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${binlog.base-path}/comments")
public class ArticleCommentController {
    private final IArticleCommentService articleCommentService;
    private final IArticleSubCommentService articleSubCommentService;

    public ArticleCommentController(IArticleCommentService articleCommentService, IArticleSubCommentService articleSubCommentService) {
        this.articleCommentService = articleCommentService;
        this.articleSubCommentService = articleSubCommentService;
    }

    @PostMapping
    public ArticleCommentVo comment(@RequestBody @Validated ArticleCommentDto dto){
        return articleCommentService.comment(dto);
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

    @GetMapping("{commentId}/sub-comments")
    public Page<ArticleCommentVo> getPage(@PathVariable Long commentId, @PageableDefault(sort = "createdDate") Pageable pageable){
        return articleSubCommentService.getPage(commentId, pageable);
    }
}
