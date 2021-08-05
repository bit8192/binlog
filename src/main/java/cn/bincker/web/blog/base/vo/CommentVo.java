package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.Comment;
import cn.bincker.web.blog.base.entity.CommentReply;
import lombok.Data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
public class CommentVo {
    private Long id;
    private Date createdDate;
    private String content;
    private BaseUserVo createdUser;
    private Long agreedNum;
    private Long treadNum;
    private Boolean isAgreed;
    private Boolean isTrod;
    private Boolean isAnonymous;
    private List<CommentVo> replies;
    private Boolean removed;
    private Collection<BaseUserVo> members;
    private Long repliesNum;

    public CommentVo(Comment message) {
        this.id = message.getId();
        this.createdDate = message.getCreatedDate();
        this.content = message.getContent();
        this.createdUser = message.getCreatedUser() == null ? null : new BaseUserVo(message.getCreatedUser());
        this.agreedNum = message.getAgreedNum();
        this.treadNum = message.getTreadNum();
        this.isAnonymous = message.getIsAnonymous();
        this.removed = message.getRemoved();
    }

    public CommentVo(CommentReply message) {
        this.id = message.getId();
        this.createdDate = message.getCreatedDate();
        this.content = message.getContent();
        this.createdUser = message.getCreatedUser() == null ? null : new BaseUserVo(message.getCreatedUser());
        this.agreedNum = message.getAgreedNum();
        this.treadNum = message.getTreadNum();
        this.isAnonymous = message.getIsAnonymous();
        this.removed = message.getRemoved();
    }
}
