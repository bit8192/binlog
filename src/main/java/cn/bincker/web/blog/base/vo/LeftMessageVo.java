package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.LeftMessage;
import cn.bincker.web.blog.base.entity.LeftMessageReply;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class LeftMessageVo {
    private Long id;
    private Date createdDate;
    private String content;
    private BaseUserVo createdUser;
    private Long agreedNum;
    private Long treadNum;
    private Boolean isAgreed;
    private Boolean isTrod;
    private Boolean isAnonymous;
    private List<LeftMessageVo> replies;
    private Boolean removed;

    public LeftMessageVo(LeftMessage message) {
        this.id = message.getId();
        this.createdDate = message.getCreatedDate();
        this.content = message.getContent();
        this.createdUser = message.getCreatedUser() == null ? null : new BaseUserVo(message.getCreatedUser());
        this.agreedNum = message.getAgreedNum();
        this.treadNum = message.getTreadNum();
        this.isAnonymous = message.getIsAnonymous();
        this.removed = message.getRemoved();
    }

    public LeftMessageVo(LeftMessageReply message) {
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
