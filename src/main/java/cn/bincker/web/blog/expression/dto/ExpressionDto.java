package cn.bincker.web.blog.expression.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Set;

import static cn.bincker.web.blog.base.constant.RegexpConstant.EXPRESSION_TITLE_VALUE;

@Data
public class ExpressionDto {
    @NotEmpty
    private String fileName;
    @NotEmpty
    @Pattern(regexp = EXPRESSION_TITLE_VALUE, message = "不合法")
    private String title;
    private Set<Long> tagIds;
}
