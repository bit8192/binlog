package cn.bincker.web.blog.material.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ArticleCommentDto {
    @NotEmpty
    private String content;
}
