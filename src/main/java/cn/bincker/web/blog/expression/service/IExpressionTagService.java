package cn.bincker.web.blog.expression.service;

import cn.bincker.web.blog.expression.dto.ExpressionTagDto;
import cn.bincker.web.blog.expression.vo.ExpressionTagVo;

import java.util.List;

public interface IExpressionTagService {
    ExpressionTagVo add(ExpressionTagDto dto);
    List<ExpressionTagVo> listAll();
}
