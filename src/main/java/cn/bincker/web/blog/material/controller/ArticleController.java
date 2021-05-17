package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.dto.valid.InsertValid;
import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import cn.bincker.web.blog.material.dto.ArticleDto;
import cn.bincker.web.blog.material.service.IArticleService;
import cn.bincker.web.blog.material.vo.ArticleListVo;
import cn.bincker.web.blog.material.vo.ArticleVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

@RequestMapping("${system.base-path}/article")
@RestController
public class ArticleController {
    private final IArticleService articleService;

    public ArticleController(IArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("{id}")
    public ArticleVo getDetail(@PathVariable Long id){
        return articleService.getDetail(id);
    }

    @GetMapping
    public Page<ArticleListVo> getPage(Pageable pageable){
        return articleService.pageAll(pageable);
    }

    @GetMapping("search/article-class/{id}")
    public Page<ArticleListVo> pageByArticleClass(@PathVariable Long id, Pageable pageable){
        return articleService.pageByClass(id, pageable);
    }

    @GetMapping("search/tag/{id}")
    public Page<ArticleListVo> pageByTag(@PathVariable Long id, Pageable pageable){
        return articleService.pageByTag(id, pageable);
    }

    @GetMapping("search/keywords")
    public Page<ArticleListVo> pageByKeywords(@NotEmpty String keywords, Pageable pageable){
        return articleService.pageByKeywords(keywords, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleVo add(@Validated(InsertValid.class) @RequestBody ArticleDto dto){
        return articleService.insert(dto);
    }

    @PutMapping
    public ArticleVo update(@Validated(UpdateValid.class) @RequestBody ArticleDto dto){
        return articleService.update(dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        articleService.delete(id);
    }
}
