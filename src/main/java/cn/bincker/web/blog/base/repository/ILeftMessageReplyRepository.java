package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.LeftMessage;
import cn.bincker.web.blog.base.entity.LeftMessageReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ILeftMessageReplyRepository extends JpaRepository<LeftMessageReply, Long> {
    List<LeftMessageReply> findAllByRecommendIsTrueAndCommentIdIn(Iterable<Long> leftMessageIds);

    Page<LeftMessageReply> findAllByCommentId(Long leftMessageId, Pageable pageable);

    Long countByComment(LeftMessage target);
}
