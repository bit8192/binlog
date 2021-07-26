package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.Message;

public interface UnreadMessageCount {
    Message.Type getType();
    Long getCount();
}
