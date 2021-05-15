package cn.bincker.web.blog.material.service.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ArticleClassPostDto {
    @NotEmpty
    @Size(max = 255)
    private String title;

    @NotNull
    private Boolean visible;

    private Integer orderNum;

    private Long parentId;
}
