package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.material.entity.ArticleClass;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.service.IArticleClassService;
import cn.bincker.web.blog.material.service.dto.ArticleClassPostDto;
import cn.bincker.web.blog.material.service.dto.ArticleClassPutDto;
import cn.bincker.web.blog.material.service.vo.ArticleClassVo;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleClassServiceImpl implements IArticleClassService {
    private final IArticleClassRepository repository;

    public ArticleClassServiceImpl(IArticleClassRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ArticleClassVo> findAllByParentId(@Nullable Long id) {
        if(id == null) return repository.listTopNode();
        return repository.findAllByParentId(id);
    }

    @Override
    public ArticleClassVo add(ArticleClassPostDto articleClass) {
        ArticleClass target = new ArticleClass();
        target.setTitle(articleClass.getTitle());
        target.setOrderNum(articleClass.getOrderNum());
        target.setVisible(articleClass.getVisible());

        ArticleClass parent = new ArticleClass();
        parent.setId(articleClass.getParentId());
        target.setParent(parent);

        return new ArticleClassVo(repository.save(target), 0L);
    }

    @Override
    public Optional<ArticleClassVo> getById(Long id) {
        return repository.findOneVo(id);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        return repository.existsById(id);
    }

    @Override
    public ArticleClassVo save(ArticleClassPutDto articleClassPutDto) {
        Optional<ArticleClass> articleClassOptional = repository.findById(articleClassPutDto.getId());
        if(articleClassOptional.isEmpty()) throw new NotFoundException();
        ArticleClass target = articleClassOptional.get();
        target.setTitle(articleClassPutDto.getTitle());
        target.setVisible(articleClassPutDto.getVisible());
        target.setOrderNum(articleClassPutDto.getOrderNum());
        target.getParent().setId(articleClassPutDto.getParentId());
        repository.save(target);
        return new ArticleClassVo(target, repository.countChildren(target.getId()));
    }
}
