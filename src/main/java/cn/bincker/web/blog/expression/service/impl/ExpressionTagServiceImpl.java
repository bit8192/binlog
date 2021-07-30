package cn.bincker.web.blog.expression.service.impl;

import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.vo.EntityLongValueVo;
import cn.bincker.web.blog.expression.dto.ExpressionTagDto;
import cn.bincker.web.blog.expression.entity.ExpressionTag;
import cn.bincker.web.blog.expression.repository.IExpressionTagRepository;
import cn.bincker.web.blog.expression.service.IExpressionTagService;
import cn.bincker.web.blog.expression.vo.ExpressionTagVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpressionTagServiceImpl implements IExpressionTagService {
    private final IExpressionTagRepository expressionTagRepository;

    public ExpressionTagServiceImpl(IExpressionTagRepository expressionTagRepository) {
        this.expressionTagRepository = expressionTagRepository;
    }

    @Override
    public ExpressionTagVo add(ExpressionTagDto dto) {
        var title = dto.getTitle();
        if(!StringUtils.hasText(title)) throw new BadRequestException("空标题", "标题不能为空");
        var tag = new ExpressionTag();
        tag.setTitle(title.trim());
        expressionTagRepository.save(tag);
        return new ExpressionTagVo(tag);
    }

    @Override
    public List<ExpressionTagVo> listAll() {
        var result = expressionTagRepository.findAll();
        var expressionTotalMap = expressionTagRepository.selectExpressionTotal()
                .stream().collect(Collectors.toUnmodifiableMap(EntityLongValueVo::getId, EntityLongValueVo::getValue));
        return result.stream().map(t->{
            var vo = new ExpressionTagVo(t);
            vo.setExpressionNum(expressionTotalMap.get(t.getId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
