package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.CommentReplyAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICommentReplyAgreeRepository extends JpaRepository<CommentReplyAgree, Long> {
    List<CommentReplyAgree> findAllByCreatedUserAndCommentIdIn(BaseUser user, Iterable<Long> recommendRepliesIds);

    Optional<CommentReplyAgree> findByCreatedUserAndCommentId(BaseUser user, Long id);

    long countByCommentId(Long id);

    Long countByCommentCreatedUser(BaseUser user);
}
