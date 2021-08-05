package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

/**
 * 留言
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
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
}
