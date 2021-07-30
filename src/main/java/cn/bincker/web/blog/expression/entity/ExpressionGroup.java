package cn.bincker.web.blog.expression.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners({UserAuditingListener.class})
public class ExpressionGroup extends BaseEntity {
    private String title;

    @OneToMany
    @JoinTable(name = "expression_group_expression")
    private List<Expression> expressionList;

    @ManyToOne
    private BaseUser createdUser;
}
