package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.service.IMessageService;
import cn.bincker.web.blog.base.vo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@ApiController
public class MessageController {
    private final IMessageService messageService;

    public MessageController(IMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("unread-count")
    public Map<Message.Type, Long> getUnreadCount(@NonNull BaseUser user){
        return messageService.getUnreadCount(user);
    }

    /**
     * 获取文章评论消息
     */
    @GetMapping("article-comment")
    public Page<CommentMessageVo> getArticleCommentMessagePage(@NonNull BaseUser user, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getArticleCommentMessagePage(user, pageable);
    }

    /**
     * 留言消息
     */
    @GetMapping("left-message")
    public Page<CommentMessageVo> getLeftMessagePage(@NotNull BaseUser user, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getLeftMessagePage(user, pageable);
    }

    /**
     * 回复消息列表
     */
    @GetMapping("reply")
    public Page<CommentMessageVo> getReplyMessagePage(@NonNull BaseUser user, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getReplyMessagePage(user, pageable);
    }

    /**
     * \@到我的消息列表
     */
    @GetMapping("mention")
    public Page<CommentMessageVo> getMentionMessagePage(@NonNull BaseUser user, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getMentionMessagePage(user, pageable);
    }

    /**
     * 收到的赞的消息列表
     */
    @GetMapping("agree")
    public Page<MessageVo> getAgreeMessagePage(@NonNull BaseUser user, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getAgreeMessagePage(user, pageable);
    }

    /**
     * 系统消息列表
     */
    @GetMapping("system")
    public Page<MessageVo> getSystemMessagePage(@NonNull BaseUser user, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getSystemMessagePage(user, pageable);
    }

    /**
     * 私信会话列表
     */
    @GetMapping("private-message-session")
    public List<PrivateMessageSessionVo> getPrivateMessageSessionPage(@NonNull BaseUser user){
        return messageService.getPrivateMessageSessionList(user);
    }

    /**
     * 私信消息列表
     * @param user 当前用户
     * @param uid 对方用户id
     */
    @GetMapping("private-message/{uid}")
    public Page<MessageVo> getPrivateMessagePage(@NonNull BaseUser user, @PathVariable Long uid, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return messageService.getPrivateMessagePage(user.getId(), uid, pageable);
    }

    /**
     * 设置已读
     * @param ids 消息id列表
     */
    @PostMapping("set-read")
    public ValueVo<Boolean> setRead(@NonNull BaseUser user, @RequestBody Long[] ids){
        return messageService.setRead(user, ids);
    }
}
