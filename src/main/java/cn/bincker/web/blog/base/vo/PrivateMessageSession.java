package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.Message;

/**
 * 私信会话
 */
public interface PrivateMessageSession {
    Message getLatestMessage();
    Long getUnreadMessageCount();
}
