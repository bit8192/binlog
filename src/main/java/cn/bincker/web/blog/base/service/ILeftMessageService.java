package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.dto.LeftMessageDto;
import cn.bincker.web.blog.base.vo.LeftMessageVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ILeftMessageService {
    Page<LeftMessageVo> getPage(Pageable pageable);

    Page<LeftMessageVo> getReplyPage(Long leftMessageId, Pageable pageable);

    /**
     * 留言
     */
    LeftMessageVo leavingMessage(LeftMessageDto dto);

    /**
     * 回复留言
     */
    LeftMessageVo replyLeftMessage(Long msgId, LeftMessageDto dto);

    /**
     * 删除留言
     */
    void removeLeftMessage(Long id);

    /**
     * 删除留言评论
     */
    void removeLeftMessageReply(Long id);

    /**
     * 切换留言点赞
     */
    ValueVo<Boolean> toggleLeftMessageAgree(Long id);

    /**
     * 切换留言评论点赞
     */
    ValueVo<Boolean> toggleLeftMessageReplyAgree(Long id);

    /**
     * 切换留言点踩
     */
    ValueVo<Boolean> toggleLeftMessageTread(Long id);

    /**
     * 切换评论留言点踩
     */
    ValueVo<Boolean> toggleLeftMessageReplyTread(Long id);
}
