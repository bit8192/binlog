package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.service.IArticleService;
import cn.bincker.web.blog.material.dto.ArticleDto;
import cn.bincker.web.blog.material.vo.ArticleClassVo;
import cn.bincker.web.blog.material.vo.ArticleListVo;
import cn.bincker.web.blog.material.vo.ArticleVo;
import cn.bincker.web.blog.material.vo.TagVo;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements IArticleService {
    private final UserAuditingListener userAuditingListener;
    private final IArticleRepository articleRepository;
    private final ITagRepository tagRepository;
    private final IArticleClassRepository articleClassRepository;
    private final INetDiskFileRepository netDiskFileRepository;

    public ArticleServiceImpl(UserAuditingListener userAuditingListener, IArticleRepository articleRepository, ITagRepository tagRepository, IArticleClassRepository articleClassRepository, INetDiskFileRepository netDiskFileRepository) {
        this.userAuditingListener = userAuditingListener;
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.articleClassRepository = articleClassRepository;
        this.netDiskFileRepository = netDiskFileRepository;
    }

    @Override
    public ArticleVo getDetail(Long articleId) {
        var result= articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        if(!result.getIsPublic()){
            var currentUser = userAuditingListener.getCurrentAuditor();
            if(currentUser.isEmpty()) throw new UnauthorizedException();
            if(!currentUser.get().getId().equals(result.getCreatedUser().getId())) throw new ForbiddenException();
        }
        return new ArticleVo(result);
    }

    @Override
    public Page<ArticleListVo> pageAll(Pageable pageable) {
        var currentUser = userAuditingListener.getCurrentAuditor();
        if(currentUser.isEmpty()){
            return articleRepository.findAll(handlePageable(pageable)).map(ArticleListVo::new);
        }else{
            return articleRepository.findAllWithUserId(currentUser.get().getId(), handlePageable(pageable)).map(ArticleListVo::new);
        }
    }

    @Override
    public Page<ArticleListVo> pageByKeywords(String keyword, Pageable pageable) {
        var currentUser = userAuditingListener.getCurrentAuditor();
        if(currentUser.isEmpty()) {
            return articleRepository.findAllByKeywords(keyword, handlePageable(pageable)).map(ArticleListVo::new);
        }else{
            return articleRepository.findAllByKeywordsWithUserId(currentUser.get().getId(), keyword, handlePageable(pageable)).map(ArticleListVo::new);
        }
    }

    @Override
    public Page<ArticleListVo> pageByClass(Long articleClassId, Pageable pageable) {
        var currentUser = userAuditingListener.getCurrentAuditor();
        if(currentUser.isEmpty()){
            return articleRepository.findAllByArticleClassIdAndIsPublicTrue(articleClassId, handlePageable(pageable)).map(ArticleListVo::new);
        }else{
            return articleRepository.findAllByArticleClassIdWithUserId(currentUser.get().getId(), articleClassId, handlePageable(pageable)).map(ArticleListVo::new);
        }
    }

    @Override
    public Page<ArticleListVo> pageByTag(Long articleTagId, Pageable pageable) {
        var currentUser = userAuditingListener.getCurrentAuditor();
        if(currentUser.isEmpty()){
            return articleRepository.findAllByTagsIdAndIsPublicTrue(articleTagId, handlePageable(pageable)).map(ArticleListVo::new);
        }else{
            return articleRepository.findAllByTagsIdWithUserId(currentUser.get().getId(), articleTagId, handlePageable(pageable)).map(ArticleListVo::new);
        }
    }

    private Pageable handlePageable(Pageable pageable){
        if(pageable.isPaged() && pageable.getSort() == Sort.unsorted())
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        return pageable;
    }

    private static final Sort DEFAULT_SORT = Sort.by(
            new Sort.Order(Sort.Direction.DESC, "top"),
            new Sort.Order(Sort.Direction.DESC, "recommend"),
            new Sort.Order(Sort.Direction.DESC, "orderNum"),
            new Sort.Order(Sort.Direction.DESC, "createdDate")
    );

    @Override
    public ArticleVo update(ArticleDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var target = articleRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
        if(!currentUser.getId().equals(target.getCreatedUser().getId())) throw new ForbiddenException();
        checkDto(dto);
        copyProperties(dto, target);
        return getUpdatedResult(articleRepository.save(target));
    }

    @Override
    public ArticleVo insert(ArticleDto dto) {
        var target = new Article();
        checkDto(dto);
        copyProperties(dto, target);
        return getUpdatedResult(articleRepository.save(target));
    }

    private void checkDto(ArticleDto dto){
        var cover = netDiskFileRepository.findById(dto.getCover().getId()).orElseThrow(NotFoundException::new);
        if(cover.getIsDirectory()) throw new BadRequestException("封面不能是文件夹");
    }

    private ArticleVo getUpdatedResult(Article target){
        var result = new ArticleVo(target);
        //查出关联对象
        result.setTags(
                tagRepository.findAllById(
                        target.getTags().stream().map(Tag::getId).collect(Collectors.toSet())
                )
                        .stream().map(tag -> new TagVo(tag, null)).collect(Collectors.toSet())
        );
        result.setCover(new NetDiskFileListVo(netDiskFileRepository.getOne(target.getCover().getId()), null));
        result.setArticleClass(new ArticleClassVo(articleClassRepository.getOne(target.getArticleClass().getId()), null));
        return result;
    }

    private void copyProperties(ArticleDto dto, Article target){
        target.setTitle(dto.getTitle());
        target.setContent(dto.getContent());
        if(StringUtils.hasText(dto.getContent())){
            Matcher matcher = RegexpConstant.MARKDOWN_IMAGE.matcher(dto.getContent());
            var result = new ArrayList<String>();
            while (matcher.find()) result.add(matcher.group(1));
            target.setImages(result.toArray(new String[]{}));
        }
        target.setArticleClass(dto.getArticleClass());
        target.setTags(dto.getTags());
        target.setCover(dto.getCover());
        target.setDescribe(dto.getDescribe());
        target.setOrderNum(dto.getOrderNum());
        target.setTop(dto.getTop());
        target.setIsOriginal(dto.getIsOriginal());
        target.setIsPublic(dto.getIsPublic());
        target.setRecommend(dto.getRecommend());
    }

    @Override
    public void delete(Long articleId) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var target = articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        if(!currentUser.getId().equals(target.getCreatedUser().getId())) throw new ForbiddenException();
        articleRepository.deleteById(articleId);
    }
}
