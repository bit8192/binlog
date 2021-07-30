package cn.bincker.web.blog.expression.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class ExpressionTagDto {
    @Pattern(regexp = "^[\\w\\u4e00-\\u9fa5]+$")
    @NotEmpty
    private String title;
}
