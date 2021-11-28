package cn.bincker.web.blog.expression.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.config.SystemFileProperties;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.enumeration.FileSystemTypeEnum;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.base.vo.SuccessMsgVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.expression.dto.ExpressionDto;
import cn.bincker.web.blog.expression.service.IExpressionService;
import cn.bincker.web.blog.expression.vo.ExpressionVo;
import cn.bincker.web.blog.utils.ResponseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping("/expressions")
@ApiController
public class ExpressionController {
    private final IExpressionService expressionService;
    private final SystemFileProperties systemFileProperties;
    private final ISystemFileFactory systemFileFactory;

    public ExpressionController(IExpressionService expressionService, SystemFileProperties systemFileProperties, ISystemFileFactory systemFileFactory) {
        this.expressionService = expressionService;
        this.systemFileProperties = systemFileProperties;
        this.systemFileFactory = systemFileFactory;
    }

    @GetMapping
    public Page<ExpressionVo> getPage(String keywords,@RequestParam(value = "tagIds", required = false) Collection<Long> tagIds, @PageableDefault(sort = "agreedNum", direction = Sort.Direction.DESC) Pageable pageable){
        if(tagIds == null) tagIds = Collections.emptyList();
        return expressionService.page(keywords, tagIds, pageable);
    }

    @PostMapping
    public ResponseEntity<Object> upload(MultipartHttpServletRequest request, @RequestPart("expressionInfos") Collection<ExpressionDto> expressionInfos){
        for (ExpressionDto expressionInfo : expressionInfos) {
            if(!expressionInfo.getTitle().matches(RegexpConstant.EXPRESSION_TITLE_VALUE)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SuccessMsgVo(false, "标题不合法"));
            }
        }
        return ResponseEntity.ok(expressionService.upload(request.getMultiFileMap(), expressionInfos));
    }

    @GetMapping(value = "{title}", produces = {MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp","image/*"})
    public void get(@PathVariable String title, HttpServletRequest request, HttpServletResponse response) throws IOException {
        var target = expressionService.getByTitle(title);
        if(!systemFileProperties.getType().equals(FileSystemTypeEnum.LOCAL)) {
            response.sendRedirect(systemFileFactory.getDownloadUrl(target.getPath()));
            return;
        }
        if(ResponseUtils.checkETag(request, response, target.getSha256())) return;
        if(ResponseUtils.checkLastModified(request, response, target.getLastModifiedDate())) return;
        ResponseUtils.setCachePeriod(response, Duration.ofDays(30));
        try(var in = systemFileFactory.fromPath(target.getPath()).getInputStream()){
            in.transferTo(response.getOutputStream());
        }
    }

    @GetMapping(value = "{title}", produces = "application/json")
    public ExpressionVo getJson(@PathVariable String title){
        return new ExpressionVo(expressionService.getByTitle(title));
    }

    @PostMapping("{id}/toggle-agree")
    public ValueVo<Boolean> toggleAgree(@PathVariable Long id){
        return expressionService.toggleAgree(id);
    }
}
