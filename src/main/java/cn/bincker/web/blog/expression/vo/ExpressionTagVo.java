package cn.bincker.web.blog.expression.vo;

import cn.bincker.web.blog.expression.entity.ExpressionTag;
import lombok.Data;

@Data
public class ExpressionTagVo {
    private Long id;
    private String title;
    private Long expressionNum;

    public ExpressionTagVo(ExpressionTag tag) {
        this.id = tag.getId();
        this.title = tag.getTitle();
    }
}
