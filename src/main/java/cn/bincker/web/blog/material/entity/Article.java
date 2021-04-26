package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.entity.converter.StringArrayConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Article extends AuditEntity {
    @NotEmpty
    private String title;

    /**
     * 是否推荐
     */
    private Boolean recommend;

    /**
     * 置顶
     */
    private Boolean top;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 推荐和置顶的排序
     */
    private Integer orderNum;

    /**
     * 简述
     */
    @Column(name = "[describe]")
    private String describe;

    @ManyToMany
    private Set<Tag> tags;

    private String cover;

    @Convert(converter = StringArrayConverter.class)
    private String[] images;

    @NotNull
    @ManyToOne
    private ArticleClass articleClass;

    @NotEmpty
    @Column(columnDefinition = "text")
    private String content;

    /**
     * 是否原创
     */
    private Boolean isOriginal;

    /**
     * 浏览量
     */
    private long viewingNum;

    /**
     * 赞数量
     */
    private long agreedNum;

    /**
     * 评论量
     */
    private long commentNum;

    /**
     * 转发量
     */
    private long forwardingNum;
}
