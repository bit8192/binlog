package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.vo.RepliesTotalVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    Page<ArticleComment> findAllByTargetId(Long articleId, Pageable pageable);

    @Query("""
    select
        comment.id as commentId,
        comment.subCommentList.size as count
    from ArticleComment comment
    where comment.id in (:ids)
    """)
    List<RepliesTotalVo> getRepliesTotals(@Param("ids") List<Long> commentIds);
}
