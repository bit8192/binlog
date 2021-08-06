package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Comment;
import cn.bincker.web.blog.base.entity.CommentReply;
import cn.bincker.web.blog.base.vo.EntityLongValueVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ICommentReplyRepository extends JpaRepository<CommentReply, Long> {
    List<CommentReply> findAllByRecommendIsTrueAndCommentIdIn(Iterable<Long> commentIds);

    Page<CommentReply> findAllByCommentId(Long commentId, Pageable pageable);

    Long countByComment(Comment target);

    @Query("""
    select
        reply.comment.id as id,
        count(reply.id) as value
    from CommentReply reply
    where
        reply.comment.id in (:commentIds)
    group by reply.comment.id
    """)
    List<EntityLongValueVo> countAllByCommentIds(Iterable<Long> commentIds);

    Long countByCreatedUser(BaseUser user);
}
