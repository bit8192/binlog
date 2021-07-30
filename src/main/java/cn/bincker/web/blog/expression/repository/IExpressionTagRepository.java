package cn.bincker.web.blog.expression.repository;

import cn.bincker.web.blog.base.vo.EntityLongValueVo;
import cn.bincker.web.blog.expression.entity.ExpressionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IExpressionTagRepository extends JpaRepository<ExpressionTag, Long> {
    @Query("""
    select
        tag.id as id,
        tag.expressionList.size as value
    from ExpressionTag tag
    """)
    List<EntityLongValueVo> selectExpressionTotal();
}
