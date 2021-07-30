package cn.bincker.web.blog.expression.service;

import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.expression.dto.ExpressionDto;
import cn.bincker.web.blog.expression.entity.Expression;
import cn.bincker.web.blog.expression.vo.ExpressionVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface IExpressionService {
    Page<ExpressionVo> page(String keyword, Collection<Long> tagIds, Pageable pageable);
    List<ExpressionVo> upload(MultiValueMap<String, MultipartFile> fileMap, Collection<ExpressionDto> expressionInfos);
    Expression getById(Long id);
    Expression getByTitle(String title);
    ValueVo<Boolean> toggleAgree(Long id);
}
