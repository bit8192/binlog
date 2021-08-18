package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Comment;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.event.MessageEvent;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.service.ICommentService;
import cn.bincker.web.blog.base.vo.CommentVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.entity.ArticleAgree;
import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.IArticleAgreeRepository;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.service.IArticleService;
import cn.bincker.web.blog.material.dto.ArticleDto;
import cn.bincker.web.blog.material.specification.ArticleSpecification;
import cn.bincker.web.blog.material.vo.ArticleClassVo;
import cn.bincker.web.blog.material.vo.ArticleListVo;
import cn.bincker.web.blog.material.vo.ArticleVo;
import cn.bincker.web.blog.material.vo.TagVo;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements IArticleService {

    private final UserAuditingListener userAuditingListener;
    private final IArticleRepository articleRepository;
    private final ITagRepository tagRepository;
    private final IArticleClassRepository articleClassRepository;
    private final INetDiskFileRepository netDiskFileRepository;
    private final IArticleAgreeRepository articleAgreeRepository;
    private final ICommentReplyRepository commentReplyRepository;
    private final ICommentAgreeRepository commentAgreeRepository;
    private final ICommentTreadRepository commentTreadRepository;
    private final ICommentReplyAgreeRepository commentReplyAgreeRepository;
    private final ICommentReplyTreadRepository commentReplyTreadRepository;
    private final IBaseUserRepository userRepository;
    private final ICommentRepository commentRepository;
    private final ApplicationContext applicationContext;

    public ArticleServiceImpl(UserAuditingListener userAuditingListener, IArticleRepository articleRepository, ITagRepository tagRepository, IArticleClassRepository articleClassRepository, INetDiskFileRepository netDiskFileRepository, IArticleAgreeRepository articleAgreeRepository, ICommentReplyRepository commentReplyRepository, ICommentAgreeRepository commentAgreeRepository, ICommentTreadRepository commentTreadRepository, ICommentReplyAgreeRepository commentReplyAgreeRepository, ICommentReplyTreadRepository commentReplyTreadRepository, IBaseUserRepository userRepository, ICommentRepository commentRepository, ApplicationContext applicationContext) {
        this.userAuditingListener = userAuditingListener;
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.articleClassRepository = articleClassRepository;
        this.netDiskFileRepository = netDiskFileRepository;
        this.articleAgreeRepository = articleAgreeRepository;
        this.commentReplyRepository = commentReplyRepository;
        this.commentAgreeRepository = commentAgreeRepository;
        this.commentTreadRepository = commentTreadRepository;
        this.commentReplyAgreeRepository = commentReplyAgreeRepository;
        this.commentReplyTreadRepository = commentReplyTreadRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public ArticleVo getDetail(Long articleId) {
        var currentUser = userAuditingListener.getCurrentAuditor();
        var article= articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        if(!article.getIsPublic()){
            if(currentUser.isEmpty()) throw new UnauthorizedException();
            if(!currentUser.get().getId().equals(article.getCreatedUser().getId())) throw new ForbiddenException();
        }
        var result = new ArticleVo(article);
        result.setCommentNum(articleRepository.countCommentNum(articleId));
        if(currentUser.isPresent()){
            result.setIsAgreed(articleAgreeRepository.findByArticleIdAndCreatedUserId(article.getId(), currentUser.get().getId()).isPresent());
        }else{
            result.setIsAgreed(false);
        }
        return result;
    }

    @Override
    public Page<ArticleListVo> pageAll(String keywords, Long articleClassId, Long[] tagIds, Pageable pageable) {
        var currentUser = userAuditingListener.getCurrentAuditor();
        var predicate = ArticleSpecification.keyWords(keywords)
                .and(ArticleSpecification.articleClass(articleClassId))
                .and(ArticleSpecification.tagIds(tagIds));
        if(currentUser.isEmpty()){
            predicate = predicate.and(ArticleSpecification.isPublic());
        }else{
            predicate = predicate.and(ArticleSpecification.publicOrUser(currentUser.get()));
        }
//        默认排序
        if(pageable.getSort().isUnsorted()){
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(
                    Sort.Order.desc("top"),
                    Sort.Order.asc("orderNum"),
                    Sort.Order.desc("createdDate")
            ));
        }
        var page = articleRepository.findAll(predicate, pageable);
        return handleIsAgreed(currentUser, page.map(ArticleListVo::new));
    }

    /**
     * 处理分页文章中是否已点赞
     * @param userOptional 用户
     * @param articlePage 文章分页
     */
    private Page<ArticleListVo> handleIsAgreed(Optional<BaseUser> userOptional, Page<ArticleListVo> articlePage){
        if(userOptional.isEmpty()){
            articlePage.forEach(a->a.setIsAgreed(false));
            return articlePage;
        }
        var articleAgreeMap = articleAgreeRepository.findAllByCreatedUserIdAndArticleIdIn(
                userOptional.get().getId(),
                articlePage.map(ArticleListVo::getId).stream().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(a->a.getArticle().getId(), a->a));
        articlePage.forEach(a->a.setIsAgreed(articleAgreeMap.containsKey(a.getId())));
        return articlePage;
    }

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
        result.setCover(new NetDiskFileVo(netDiskFileRepository.getOne(target.getCover().getId())));
        result.setArticleClass(new ArticleClassVo(articleClassRepository.getOne(target.getArticleClass().getId()), null));
        return result;
    }

    private void copyProperties(ArticleDto dto, Article target){
        target.setTitle(dto.getTitle());
        target.setContent(dto.getContent());
        if(StringUtils.hasText(dto.getContent())){
            Matcher matcher = RegexpConstant.MARKDOWN_IMAGE.matcher(dto.getContent());
            var result = new ArrayList<String>();
            var index = 0;
            while (matcher.find() && index++ < 5) result.add(matcher.group(1));
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

    @Override
    @Transactional
    public ValueVo<Boolean> toggleAgreed(Long articleId) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var article = articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        var articleAgreeOptional = articleAgreeRepository.findByArticleIdAndCreatedUserId(articleId, currentUser.getId());
        if(articleAgreeOptional.isEmpty()){
            var articleAgree = new ArticleAgree();
            articleAgree.setArticle(article);
            articleAgreeRepository.save(articleAgree);
//            触发点赞消息
            if(!currentUser.getId().equals(article.getCreatedUser().getId())) {
                var messageEvent = new MessageEvent(
                        applicationContext,
                        null,
                        Message.Type.ARTICLE_AGREE,
                        currentUser,
                        article.getCreatedUser(),
                        null,
                        article.getId(),
                        articleAgree.getId()
                );
                applicationContext.publishEvent(messageEvent);
            }
        }else{
            articleAgreeRepository.deleteById(articleAgreeOptional.get().getId());
        }
        synchronized ((SynchronizedPrefixConstant.UPDATE_ARTICLE_AGREE + articleId).intern()){
            article.setAgreedNum(articleAgreeRepository.countByArticleId(articleId));
            articleRepository.save(article);
        }
        return new ValueVo<>(articleAgreeOptional.isEmpty());
    }

    @Override
    public synchronized void view(Long articleId) {
        var article = articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        article.setViewingNum(article.getViewingNum() + 1);
        articleRepository.save(article);
    }

    @Override
    public Page<CommentVo> getCommentPage(Long articleId, Pageable pageable) {
        var commentPage = articleRepository.selectCommentPage(articleId, pageable);
        var userOptional = userAuditingListener.getCurrentAuditor();
        var voPage = ICommentService.handleComment(commentReplyRepository, commentAgreeRepository, commentTreadRepository, commentReplyAgreeRepository, commentReplyTreadRepository, userRepository, userOptional, commentPage);
        ICommentService.handleMember(userRepository, voPage);
        return voPage;
    }

    @Override
    @Transactional
    public CommentVo commenting(Long articleId, CommentDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var article = articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        dto.setIsAnonymous(false);//文章评论不能匿名
        var comment = ICommentService.commenting(commentRepository, Comment.Type.ARTICLE, dto);
        article.getComments().add(comment);
        articleRepository.save(article);
        var vo = new CommentVo(comment);
        vo.setIsAgreed(false);
        vo.setIsTrod(false);
        ICommentService.handleMember(userRepository, Collections.singletonList(vo));
//        触发消息
        if(!currentUser.getId().equals(article.getCreatedUser().getId())) {
            var messageEvent = new MessageEvent(
                    applicationContext,
                    dto.getContent(),
                    Message.Type.ARTICLE_COMMENT,
                    comment.getCreatedUser(),
                    article.getCreatedUser(),
                    null,
                    article.getId(),
                    comment.getId()
            );
            applicationContext.publishEvent(messageEvent);
        }
        return vo;
    }
}
