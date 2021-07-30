package cn.bincker.web.blog.expression.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.expression.entity.Expression;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ExpressionVo {
    private Long id;
    private String title;
    private Set<ExpressionTagVo> tags;
    private Long agreedNum;
    private BaseUserVo createdUser;
    private Boolean isAgreed;

    public ExpressionVo(Expression expression) {
        this.id = expression.getId();
        this.title = expression.getTitle();
        this.agreedNum = expression.getAgreedNum();
        this.tags = expression.getTags().stream().map(ExpressionTagVo::new).collect(Collectors.toSet());
        this.createdUser = new BaseUserVo(expression.getCreatedUser());
    }
}
