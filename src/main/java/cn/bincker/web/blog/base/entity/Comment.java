package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.UserAuditingListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@EntityListeners(UserAuditingListener.class)
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
public class Comment<T extends BaseEntity> extends BaseEntity{
    private String content;

    /**
     * 评论对象
     */
    @ManyToOne
    @JsonIgnore
    private T target;

    private long agreedNum;

    private long treadNum;

    @ManyToOne
    @CreatedBy
    private BaseUser createdUser;
}
