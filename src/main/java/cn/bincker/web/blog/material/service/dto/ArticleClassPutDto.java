package cn.bincker.web.blog.material.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ArticleClassPutDto {
    @NotNull
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private Boolean visible;
    private Integer orderNum;
    private Long parentId;
}
