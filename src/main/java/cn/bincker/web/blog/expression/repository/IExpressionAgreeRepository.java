package cn.bincker.web.blog.expression.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.expression.entity.Expression;
import cn.bincker.web.blog.expression.entity.ExpressionAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IExpressionAgreeRepository extends JpaRepository<ExpressionAgree, Long> {
    Optional<ExpressionAgree> findByExpressionId(Long id);

    Long countByExpression(Expression expression);

    List<ExpressionAgree> findByCreatedUserAndExpressionIdIn(BaseUser user, Iterable<Long> ids);
}
