package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.CommentTread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICommentTreadRepository extends JpaRepository<CommentTread, Long> {
    List<CommentTread> findAllByCreatedUserAndCommentIdIn(BaseUser user, Iterable<Long> commentIds);

    Optional<CommentTread> findByCreatedUserAndCommentId(BaseUser user, Long id);

    Long countByCommentId(Long id);
}
