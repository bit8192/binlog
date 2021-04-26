package cn.bincker.web.blog.material.service.dto;

import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.material.entity.ArticleClass;
import cn.bincker.web.blog.material.entity.Tag;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class ArticleDto {
    private Long id;
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
    private String describe;

    private Set<Tag> tags;

    /**
     * 封面
     */
    private String cover;

    /**
     * 图片组
     */
    private String[] images;

    private ArticleClass articleClass;

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

    /**
     * 作者
     */
    private AuthorDto author;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 最后修改时间
     */
    private Date lastModifiedDate;
}
