package cn.bincker.web.blog.material.service;

import cn.bincker.web.blog.material.service.dto.TagPostDto;
import cn.bincker.web.blog.material.service.dto.TagPutDto;
import cn.bincker.web.blog.material.service.vo.TagVo;

import java.util.List;
import java.util.Optional;

public interface ITagService {
    List<TagVo> listAll();

    Optional<TagVo> findById(Long id);

    TagVo add(TagPostDto tag);

    TagVo save(TagPutDto tag);

    void delete(Long id);
}
