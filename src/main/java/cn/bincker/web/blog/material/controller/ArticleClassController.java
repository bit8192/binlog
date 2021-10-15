package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.material.service.IArticleClassService;
import cn.bincker.web.blog.material.dto.ArticleClassPostDto;
import cn.bincker.web.blog.material.dto.ArticleClassPutDto;
import cn.bincker.web.blog.material.vo.ArticleClassVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/article-classes")
public class ArticleClassController {
    private final IArticleClassService articleClassService;

    public ArticleClassController(IArticleClassService articleClassService) {
        this.articleClassService = articleClassService;
    }

    /**
     * 获取分类信息
     */
    @GetMapping("{id}")
    public ArticleClassVo get(@PathVariable Long id){
        return articleClassService.getById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * 列出子节点
     */
    @GetMapping("search/parent")
    public Collection<ArticleClassVo> findAllByParentId(Long id) {
        return articleClassService.findAllByParentId(id);
    }

    /**
     * 添加
     */
    @PostMapping
    public ArticleClassVo post(@Validated @RequestBody ArticleClassPostDto articleClass){
        return articleClassService.add(articleClass);
    }

    /**
     * 删除
     */
    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        if(!articleClassService.exists(id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        articleClassService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 修改
     */
    @PutMapping("")
    public ResponseEntity<ArticleClassVo> put(@Validated @RequestBody ArticleClassPutDto articleClassPutDto){
        if(articleClassService.exists(articleClassPutDto.getId()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(articleClassService.save(articleClassPutDto));
    }
}
