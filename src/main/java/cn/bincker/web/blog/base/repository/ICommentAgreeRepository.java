package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.CommentAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICommentAgreeRepository extends JpaRepository<CommentAgree, Long> {
    List<CommentAgree> findAllByCreatedUserAndCommentIdIn(BaseUser user, Iterable<Long> commentIds);

    Optional<CommentAgree> findByCreatedUserAndCommentId(BaseUser user, Long id);

    Long countByCommentId(Long id);

    Long countByCommentCreatedUser(BaseUser user);
}
