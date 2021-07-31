package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.LeftMessageReplyTread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILeftMessageReplyTreadRepository extends JpaRepository<LeftMessageReplyTread, Long> {
    List<LeftMessageReplyTread> findAllByCreatedUserAndMessageIdIn(BaseUser user, Iterable<Long> recommendRepliesIds);

    Optional<LeftMessageReplyTread> findByCreatedUserAndMessageId(BaseUser user, Long id);

    long countByMessageId(Long id);
}
