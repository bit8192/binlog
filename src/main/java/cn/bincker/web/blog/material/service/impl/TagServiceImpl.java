package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.service.ITagService;
import cn.bincker.web.blog.material.dto.TagPostDto;
import cn.bincker.web.blog.material.dto.TagPutDto;
import cn.bincker.web.blog.material.vo.TagVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements ITagService {
    private final ITagRepository tagRepository;

    public TagServiceImpl(ITagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<TagVo> listAll() {
        return tagRepository.findAllVo();
    }

    @Override
    public Optional<TagVo> findById(Long id) {
        return tagRepository.findVoById(id);
    }

    @Override
    public TagVo add(TagPostDto dto) {
        var tag = new Tag();
        tag.setTitle(dto.getTitle());
        tagRepository.save(tag);
        return new TagVo(tag.getId(), tag.getTitle(), 0L);
    }

    @Override
    public TagVo save(TagPutDto dto) {
        var optional = tagRepository.findById(dto.getId());
        if(optional.isEmpty()) throw new NotFoundException();
        var target = optional.get();
        target.setTitle(dto.getTitle());
        tagRepository.save(target);
        return new TagVo(target.getId(), target.getTitle(), tagRepository.countArticleNum(target.getId()));
    }

    @Override
    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}
