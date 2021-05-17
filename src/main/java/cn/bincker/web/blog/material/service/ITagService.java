package cn.bincker.web.blog.material.service;

import cn.bincker.web.blog.material.dto.TagPostDto;
import cn.bincker.web.blog.material.dto.TagPutDto;
import cn.bincker.web.blog.material.vo.TagVo;

import java.util.List;
import java.util.Optional;

public interface ITagService {
    List<TagVo> listAll();

    Optional<TagVo> findById(Long id);

    TagVo add(TagPostDto tag);

    TagVo save(TagPutDto tag);

    void delete(Long id);
}
