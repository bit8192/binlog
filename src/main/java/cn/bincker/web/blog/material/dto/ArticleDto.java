package cn.bincker.web.blog.material.dto;

import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import cn.bincker.web.blog.material.entity.ArticleClass;
import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class ArticleDto {
    @NotNull(groups = {UpdateValid.class})
    private Long id;

    @NotEmpty
    private String title;

    /**
     * 是否推荐
     */
    @NotNull
    private Boolean recommend;

    /**
     * 置顶
     */
    @NotNull
    private Boolean top;

    /**
     * 是否公开
     */
    @NotNull
    private Boolean isPublic;

    /**
     * 是否原创
     */
    @NotNull
    private Boolean isOriginal;

    /**
     * 推荐和置顶的排序
     */
    private Integer orderNum = 0;

    /**
     * 关键字
     */
    private String keywords;

    /**
     * 简述
     */
    @NotEmpty
    private String describe;

    private Set<Tag> tags;

    /**
     * 封面
     */
    private NetDiskFile cover;

    @NotNull
    private ArticleClass articleClass;

    @NotEmpty
    private String content;
}
