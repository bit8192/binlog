package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.CommentReplyTread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICommentReplyTreadRepository extends JpaRepository<CommentReplyTread, Long> {
    List<CommentReplyTread> findAllByCreatedUserAndCommentIdIn(BaseUser user, Iterable<Long> recommendRepliesIds);

    Optional<CommentReplyTread> findByCreatedUserAndCommentId(BaseUser user, Long id);

    long countByCommentId(Long id);
}
