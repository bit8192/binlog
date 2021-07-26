package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.dto.MessageDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.vo.CommentMessageVo;
import cn.bincker.web.blog.base.vo.MessageVo;
import cn.bincker.web.blog.base.vo.PrivateMessageSessionVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IMessageService {
    /**
     * 获取用户未读消息统计
     * @param baseUser 用户
     */
    Map<Message.Type, Long> getUnreadCount(BaseUser baseUser);

    /**
     * 文章评论消息
     * @return
     */
    Page<CommentMessageVo> getArticleCommentMessagePage(BaseUser baseUser, Pageable pageable);

    /**
     * 获取回复消息列表
     * @param baseUser 用户
     * @param pageable 分页
     * @return
     */
    Page<CommentMessageVo> getReplyMessagePage(BaseUser baseUser, Pageable pageable);

    /**
     * 获取@到我的消息列表
     * @param baseUser 用户
     * @param pageable 分页
     * @return
     */
    Page<CommentMessageVo> getMentionMessagePage(BaseUser baseUser, Pageable pageable);

    /**
     * 获取收到的赞消息列表
     * @param baseUser 用户
     * @param pageable 分页
     */
    Page<MessageVo> getAgreeMessagePage(BaseUser baseUser, Pageable pageable);

    /**
     * 获取系统消息列表
     */
    Page<MessageVo> getSystemMessagePage(BaseUser baseUser, Pageable pageable);

    /**
     * 查询私信会话列表
     */
    List<PrivateMessageSessionVo> getPrivateMessageSessionList(BaseUser baseUser);

    /**
     * 对话信息列表
     */
    Page<MessageVo> getPrivateMessagePage(Long myId, Long hisId, Pageable pageable);

    /**
     * 发送私信消息
     */
    MessageVo sendMessage(BaseUser fromUser, MessageDto dto);

    /**
     * 批量设置消息已读
     */
    ValueVo<Boolean> setRead(BaseUser user, Long... ids);
}
