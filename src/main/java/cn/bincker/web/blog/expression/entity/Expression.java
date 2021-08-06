package cn.bincker.web.blog.expression.entity;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@EntityListeners(UserAuditingListener.class)
@NamedEntityGraph(name = "Expression.all", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("createdUser"),
})
@NamedEntityGraph(name = "Expression.none")
public class Expression extends BaseEntity {
    @Column(unique = true)
    private String title;

    private String path;

    @Column(unique = true)
    private String sha256;

    @ManyToMany
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "expression_tags", joinColumns = {@JoinColumn(name = "expression_id")}, inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private Set<ExpressionTag> tags;

    @CreatedBy
    @ManyToOne
    @Fetch(FetchMode.JOIN)
    private BaseUser createdUser;

    private Long agreedNum;
}
