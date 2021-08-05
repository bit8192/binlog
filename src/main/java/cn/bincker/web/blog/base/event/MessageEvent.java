package cn.bincker.web.blog.base.event;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class MessageEvent extends ApplicationEvent {
    @Getter
    private final String content;
    @Getter
    private final Message.Type type;
    @Getter
    private final BaseUser fromUser;
    @Getter
    private final BaseUser toUser;
    @Getter
    private final Long relatedTargetId;
    @Getter
    private final Long originalTargetId;
    @Getter
    private final Long targetId;

    public MessageEvent(Object source, String content, Message.Type type, BaseUser fromUser, BaseUser toUser, Long relatedTargetId, Long originalTargetId, Long targetId) {
        super(source);
        this.content = content;
        this.type = type;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.relatedTargetId = relatedTargetId;
        this.originalTargetId = originalTargetId;
        this.targetId = targetId;
    }
}
