package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentMessageVo extends MessageVo{
    private Boolean isAgreed;
    private List<BaseUserVo> members;
    public CommentMessageVo(Message message) {
        super(message);
    }
}
