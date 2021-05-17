package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.material.service.IArticleClassService;
import cn.bincker.web.blog.material.service.dto.ArticleClassPostDto;
import cn.bincker.web.blog.material.service.dto.ArticleClassPutDto;
import cn.bincker.web.blog.material.service.vo.ArticleClassVo;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("${system.base-path}/article-classes")
public class ArticleClassController {
    private final IArticleClassService articleClassService;

    public ArticleClassController(IArticleClassService articleClassService) {
        this.articleClassService = articleClassService;
    }

    /**
     * 获取分类信息
     */
    @GetMapping("{id}")
    public RepresentationModel<EntityModel<ArticleClassVo>> get(@PathVariable Long id){
        Optional<ArticleClassVo> result = articleClassService.getById(id);
        return HalModelBuilder.halModelOf(result).build();
    }

    /**
     * 列出子节点
     */
    @GetMapping("search/parent")
    public RepresentationModel<CollectionModel<ArticleClassVo>> findAllByParentId(Long id) {
        return HalModelBuilder
                .halModel()
                .embed(articleClassService.findAllByParentId(id), LinkRelation.of("articleClassVo"))
                .link(linkTo(methodOn(ArticleClassController.class).findAllByParentId(id)).withSelfRel())
                .build();
    }

    /**
     * 添加
     */
    @PostMapping
    public EntityModel<ArticleClassVo> post(@Validated @RequestBody ArticleClassPostDto articleClass){
        ArticleClassVo result = articleClassService.add(articleClass);
        return EntityModel.of(result);
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
    public ResponseEntity<EntityModel<ArticleClassVo>> put(@Validated @RequestBody ArticleClassPutDto articleClassPutDto){
        if(articleClassService.exists(articleClassPutDto.getId()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(EntityModel.of(articleClassService.save(articleClassPutDto)));
    }
}
