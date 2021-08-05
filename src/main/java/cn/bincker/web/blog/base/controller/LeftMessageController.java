package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.service.ICommentService;
import cn.bincker.web.blog.base.vo.CommentVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${binlog.base-path}/left-messages")
public class LeftMessageController {
    private final ICommentService commentService;

    public LeftMessageController(ICommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("{id}/reply")
    public Page<CommentVo> replyPage(@PathVariable Long id, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return commentService.getReplyPage(id, pageable);
    }

    @PostMapping
    public CommentVo leavingMessage(@RequestBody @Validated CommentDto dto, @NonNull BaseUser user){
//        var comment = ICommentService.commenting(dto);
//        var vo = new CommentVo(comment);
//        vo.setCreatedUser(new BaseUserVo(user));
//        return vo;
        return null;
    }

    @PostMapping("{id}/reply")
    public CommentVo replyLeftMessage(@PathVariable Long id, @RequestBody @Validated CommentDto dto){
//        return commentService.replyComment(id, dto);
        return null;
    }

    @DeleteMapping("{id}")
    public void removeLeftMessage(@PathVariable Long id){
        commentService.removeComment(id);
    }

    @DeleteMapping("reply/{id}")
    public void removeReply(@PathVariable Long id){
        commentService.removeReply(id);
    }

    @PostMapping("{id}/toggle-agree")
    public ValueVo<Boolean> toggleLeftMessageAgree(@PathVariable Long id){
        return commentService.toggleCommentAgree(id);
    }

    @PostMapping("{id}/toggle-tread")
    public ValueVo<Boolean> toggleLeftMessageTread(@PathVariable Long id){
        return commentService.toggleCommentTread(id);
    }

    @PostMapping("reply/{id}/toggle-agree")
    public ValueVo<Boolean> toggleLeftMessageReplyAgree(@PathVariable Long id){
        return commentService.toggleReplyAgree(id);
    }

    @PostMapping("reply/{id}/toggle-tread")
    public ValueVo<Boolean> toggleLeftMessageReplyTread(@PathVariable Long id){
        return commentService.toggleReplyTread(id);
    }
}
