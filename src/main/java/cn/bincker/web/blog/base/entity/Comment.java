package cn.bincker.web.blog.base.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

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
    private BaseUser createdUser;
}
