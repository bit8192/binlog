package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.material.service.ITagService;
import cn.bincker.web.blog.material.service.dto.TagPostDto;
import cn.bincker.web.blog.material.service.dto.TagPutDto;
import cn.bincker.web.blog.material.service.vo.TagVo;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${system.base-path}/tags")
public class TagController {
    private final ITagService tagService;

    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/all")
    public RepresentationModel<CollectionModel<TagVo>> list(){
        return HalModelBuilder.halModel().embed(tagService.listAll(), TagVo.class).build();
    }

    @GetMapping("{id}")
    public ResponseEntity<EntityModel<TagVo>> get(@PathVariable Long id){
        var optional = tagService.findById(id);
        if(optional.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(EntityModel.of(optional.get()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<TagVo> post(@Validated @RequestBody TagPostDto dto){
        return EntityModel.of(tagService.add(dto));
    }

    @PutMapping
    public EntityModel<TagVo> put(@Validated @RequestBody TagPutDto dto){
        return EntityModel.of(tagService.save(dto));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        tagService.delete(id);
    }
}
