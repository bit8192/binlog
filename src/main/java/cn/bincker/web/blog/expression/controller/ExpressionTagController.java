package cn.bincker.web.blog.expression.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.expression.dto.ExpressionTagDto;
import cn.bincker.web.blog.expression.service.IExpressionTagService;
import cn.bincker.web.blog.expression.vo.ExpressionTagVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expression-tags")
@ApiController
public class ExpressionTagController {
    private final IExpressionTagService expressionTagService;

    public ExpressionTagController(IExpressionTagService expressionTagService) {
        this.expressionTagService = expressionTagService;
    }

    @GetMapping
    public List<ExpressionTagVo> getAll(){
        return expressionTagService.listAll();
    }

    @PostMapping
    public ExpressionTagVo add(@RequestBody @Validated ExpressionTagDto dto){
        return expressionTagService.add(dto);
    }
}
