package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

/**
 * 留言
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EntityListeners(UserAuditingListener.class)
public class Comment extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @NotEmpty
    @Column(nullable = false)
    private String content;

    @CreatedBy
    @ManyToOne
    private BaseUser createdUser;

    @Column(nullable = false)
    private Long agreedNum;

    @Column(nullable = false)
    private Long treadNum;

    @Column(nullable = false)
    private Boolean removed;

    /**
     * 匿名评论
     */
    @Column(nullable = false)
    private Boolean isAnonymous;

    /**
     * 评论类型
     */
    public enum Type{
        /**
         * 文章
         */
        ARTICLE,
        /**
         * 留言
         */
        LEFT_MESSAGE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Comment comment = (Comment) o;

        return Objects.equals(getId(), comment.getId());
    }

    @Override
    public int hashCode() {
        return 860659860;
    }
}
