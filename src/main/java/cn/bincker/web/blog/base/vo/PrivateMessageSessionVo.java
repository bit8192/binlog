package cn.bincker.web.blog.base.vo;

import lombok.Data;

@Data
public class PrivateMessageSessionVo {
    private MessageVo latestMessage;
    private Long unreadMessageCount;

    public PrivateMessageSessionVo(PrivateMessageSession privateMessageSession) {
        this.latestMessage = new MessageVo(privateMessageSession.getLatestMessage());
        this.unreadMessageCount = privateMessageSession.getUnreadMessageCount();
    }
}
