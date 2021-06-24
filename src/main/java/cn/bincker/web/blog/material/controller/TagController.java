package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.material.service.ITagService;
import cn.bincker.web.blog.material.dto.TagPostDto;
import cn.bincker.web.blog.material.dto.TagPutDto;
import cn.bincker.web.blog.material.vo.TagVo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("${system.base-path}/tags")
public class TagController {
    private final ITagService tagService;

    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/all")
    public Collection<TagVo> list(){
        return tagService.listAll();
    }

    @GetMapping("hot")
    public Collection<TagVo> hotList(){
        return tagService.getHotList();
    }

    @GetMapping("{id}")
    public TagVo get(@PathVariable Long id){
        return tagService.findById(id).orElseThrow(NotFoundException::new);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagVo post(@Validated @RequestBody TagPostDto dto){
        return tagService.add(dto);
    }

    @PutMapping
    public TagVo put(@Validated @RequestBody TagPutDto dto){
        return tagService.save(dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        tagService.delete(id);
    }
}
