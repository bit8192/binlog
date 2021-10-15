package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.service.ICommentService;
import cn.bincker.web.blog.base.vo.CommentVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 回复评论
     */
    @PostMapping("{commentId}/replies")
    public CommentVo replyComment(@PathVariable Long commentId, @RequestBody @Validated CommentDto dto){
        return commentService.replyComment(commentId, dto);
    }

    /**
     * 回复子评论
     */
    @PostMapping("replies/{replyId}/replies")
    public CommentVo replySubComment(@PathVariable Long replyId, @RequestBody @Validated CommentDto dto){
        return commentService.replySubComment(replyId, dto);
    }

    /**
     * 回复列表
     */
    @GetMapping("{commentId}/replies")
    public Page<CommentVo> getCommentReplyPage(@PathVariable Long commentId, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return commentService.getReplyPage(commentId, pageable);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("{commentId}")
    public void removeComment(@PathVariable Long commentId){
        commentService.removeComment(commentId);
    }

    /**
     * 删除回复
     */
    @DeleteMapping("replies/{replyId}")
    public void removeReply(@PathVariable Long replyId){
        commentService.removeReply(replyId);
    }

    /**
     * 切换评论点赞
     */
    @PostMapping("{commentId}/toggle-agree")
    public ValueVo<Boolean> toggleCommentAgree(@PathVariable Long commentId){
        return commentService.toggleCommentAgree(commentId);
    }

    /**
     * 切换回复点赞
     */
    @PostMapping("replies/{replyId}/toggle-agree")
    public ValueVo<Boolean> toggleReplyAgree(@PathVariable Long replyId){
        return commentService.toggleReplyAgree(replyId);
    }

    /**
     * 切换评论点踩
     */
    @PostMapping("{commentId}/toggle-tread")
    public ValueVo<Boolean> toggleCommentTread(@PathVariable Long commentId){
        return commentService.toggleCommentTread(commentId);
    }

    /**
     * 切换回复点踩
     */
    @PostMapping("replies/{replyId}/toggle-tread")
    public ValueVo<Boolean> toggleReplyTread(@PathVariable Long replyId){
        return commentService.toggleReplyTread(replyId);
    }
}
