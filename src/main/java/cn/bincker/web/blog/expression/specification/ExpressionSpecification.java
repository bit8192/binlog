package cn.bincker.web.blog.expression.specification;

import cn.bincker.web.blog.expression.entity.Expression;
import cn.bincker.web.blog.expression.entity.ExpressionTag;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.expression.LiteralExpression;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

public class ExpressionSpecification {
    public static Specification<Expression> titleLike(String keyword){
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
    }

    public static Specification<Expression> tagIdIn(Iterable<Long> tagIds){
        return (root, query, criteriaBuilder) -> {
            if(tagIds == null) return null;
            var subQuery = query.subquery(Long.class);
            var subQueryRoot = subQuery.from(ExpressionTag.class);
            subQuery.select(subQueryRoot.get("id"));
            subQuery.where(criteriaBuilder.equal(subQueryRoot.join("expressionList").get("id"), root.get("id")));

            var tagIterator = tagIds.iterator();
            if(!tagIterator.hasNext()) return null;
            Predicate predicate = criteriaBuilder.in(new LiteralExpression<>((CriteriaBuilderImpl) criteriaBuilder, tagIterator.next())).value(subQuery);
            while (tagIterator.hasNext())
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.in(new LiteralExpression<>((CriteriaBuilderImpl) criteriaBuilder, tagIterator.next())).value(subQuery));
            return predicate;
        };
    }
}
