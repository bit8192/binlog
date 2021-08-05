package cn.bincker.web.blog.base.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class CommentReply extends BaseEntity{

    private String content;

    /**
     * 上级评论
     */
    @ManyToOne
    @JsonIgnore
    private Comment comment;

    private long agreedNum;

    private long treadNum;

    @ManyToOne
    @CreatedBy
    private BaseUser createdUser;

    private Boolean recommend;

    @Column(nullable = false)
    private Boolean removed;

    @Column(nullable = false)
    private Boolean isAnonymous;
}
