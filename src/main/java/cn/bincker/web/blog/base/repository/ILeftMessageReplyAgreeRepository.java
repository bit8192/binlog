package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.LeftMessageReplyAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILeftMessageReplyAgreeRepository extends JpaRepository<LeftMessageReplyAgree, Long> {
    List<LeftMessageReplyAgree> findAllByCreatedUserAndMessageIdIn(BaseUser user, Iterable<Long> recommendRepliesIds);

    Optional<LeftMessageReplyAgree> findByCreatedUserAndMessageId(BaseUser user, Long id);

    long countByMessageId(Long id);
}
