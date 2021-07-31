package cn.bincker.web.blog.base.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class LeftMessageReply extends BaseEntity{

    private String content;

    /**
     * 评论对象
     */
    @ManyToOne
    @JsonIgnore
    private LeftMessage comment;

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
