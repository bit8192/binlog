package cn.bincker.web.blog.base.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LeftMessageDto {
    @NotEmpty
    public String content;

    public Boolean isAnonymous;
}
