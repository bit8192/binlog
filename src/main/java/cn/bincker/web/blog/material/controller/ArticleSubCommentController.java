package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleSubCommentDto;
import cn.bincker.web.blog.material.service.IArticleSubCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${system.base-path}/sub-comments")
public class ArticleSubCommentController {
    private final IArticleSubCommentService articleSubCommentService;

    public ArticleSubCommentController(IArticleSubCommentService articleSubCommentService) {
        this.articleSubCommentService = articleSubCommentService;
    }

    @PostMapping
    public ArticleCommentVo comment(@RequestBody @Validated ArticleSubCommentDto dto){
        return articleSubCommentService.comment(dto);
    }

    /**
     * 对子评论进行评论
     */
    @PostMapping("sub-comments")
    public ArticleCommentVo subComment(@RequestBody @Validated ArticleSubCommentDto dto){
        return articleSubCommentService.subComment(dto);
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

}
