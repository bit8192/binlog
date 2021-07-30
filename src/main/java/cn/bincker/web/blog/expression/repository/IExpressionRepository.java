package cn.bincker.web.blog.expression.repository;

import cn.bincker.web.blog.expression.entity.Expression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IExpressionRepository extends JpaRepository<Expression, Long>, JpaSpecificationExecutor<Expression> {
    @EntityGraph("Expression.none")
    Optional<Expression> findByTitle(String title);

    @EntityGraph("Expression.all")
    @Override
    Page<Expression> findAll(Specification<Expression> spec, Pageable pageable);

    @EntityGraph("Expression.none")
    Optional<Expression> findBySha256(String sha256);
}
