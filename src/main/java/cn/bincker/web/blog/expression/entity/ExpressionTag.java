package cn.bincker.web.blog.expression.entity;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@EntityListeners(UserAuditingListener.class)
public class ExpressionTag extends BaseEntity {
    @Column(unique = true)
    private String title;

    @ManyToMany(mappedBy = "tags")
    private List<Expression> expressionList;

    @CreatedBy
    @ManyToOne
    private BaseUser createdUser;
}
