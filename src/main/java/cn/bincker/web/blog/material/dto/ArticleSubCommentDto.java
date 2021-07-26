package cn.bincker.web.blog.material.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ArticleSubCommentDto {
    @NotNull
    private Long commentId;

    @NotEmpty
    private String content;
}
