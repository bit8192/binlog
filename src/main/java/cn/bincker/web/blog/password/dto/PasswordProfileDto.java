package cn.bincker.web.blog.password.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class PasswordProfileDto {
    private Long id;

    @NotEmpty
    private String inputConvertJs;
}
