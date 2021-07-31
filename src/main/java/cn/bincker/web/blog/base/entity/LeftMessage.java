package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;

/**
 * 留言
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@EntityListeners(UserAuditingListener.class)
public class LeftMessage extends BaseEntity {
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
}
