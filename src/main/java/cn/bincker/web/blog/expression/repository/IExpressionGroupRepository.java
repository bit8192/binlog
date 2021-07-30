package cn.bincker.web.blog.expression.repository;

import cn.bincker.web.blog.expression.entity.ExpressionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IExpressionGroupRepository extends JpaRepository<ExpressionGroup, Long> {
}
