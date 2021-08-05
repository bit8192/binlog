package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ICommentRepository extends JpaRepository<Comment, Long> {
}
