package cn.bincker.web.blog.base.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class MessageDto {
    @NotEmpty
    private String content;

    @NotNull
    private Long toUserId;
}
