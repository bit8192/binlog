package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentMessageVo extends MessageVo{

    private Boolean isAgreed;

    private List<BaseUserVo> members;

    private Boolean isAnonymous;

    private Boolean removed;

    public CommentMessageVo() {
    }

    public CommentMessageVo(Message message) {
        super(message);
    }
}
