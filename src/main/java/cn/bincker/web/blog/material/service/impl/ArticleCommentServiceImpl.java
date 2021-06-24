package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.entity.ArticleCommentAgree;
import cn.bincker.web.blog.material.entity.ArticleCommentTread;
import cn.bincker.web.blog.material.repository.*;
import cn.bincker.web.blog.material.service.IArticleCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentListVo;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import cn.bincker.web.blog.material.vo.RepliesTotalVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class ArticleCommentServiceImpl implements IArticleCommentService {
    private static final Logger log = LoggerFactory.getLogger(ArticleCommentServiceImpl.class);

    private final IArticleCommentRepository articleCommentRepository;
    private final UserAuditingListener userAuditingListener;
    private final IArticleRepository articleRepository;
    private final IArticleCommentAgreeRepository articleCommentAgreeRepository;
    private final IArticleCommentTreadRepository articleCommentTreadRepository;
    private final IArticleSubCommentRepository articleSubCommentRepository;
    private final IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository;
    private final IArticleSubCommentTreadRepository articleSubCommentTreadRepository;

    public ArticleCommentServiceImpl(IArticleCommentRepository articleCommentRepository, UserAuditingListener userAuditingListener, IArticleRepository articleRepository, IArticleCommentAgreeRepository articleCommentAgreeRepository, IArticleCommentTreadRepository articleCommentTreadRepository, IArticleSubCommentRepository articleSubCommentRepository, IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository, IArticleSubCommentTreadRepository articleSubCommentTreadRepository) {
        this.articleCommentRepository = articleCommentRepository;
        this.userAuditingListener = userAuditingListener;
        this.articleRepository = articleRepository;
        this.articleCommentAgreeRepository = articleCommentAgreeRepository;
        this.articleCommentTreadRepository = articleCommentTreadRepository;
        this.articleSubCommentRepository = articleSubCommentRepository;
        this.articleSubCommentAgreeRepository = articleSubCommentAgreeRepository;
        this.articleSubCommentTreadRepository = articleSubCommentTreadRepository;
    }

    @Override
    public ArticleCommentVo comment(Long articleId, ArticleCommentDto dto) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var article = articleRepository.findById(articleId).orElseThrow(NotFoundException::new);
        var comment = new ArticleComment();
        comment.setContent(dto.getContent());
        comment.setTarget(article);
        articleCommentRepository.save(comment);
        comment.setCreatedUser(user);
        return new ArticleCommentVo(comment);
    }

    @Override
    public void del(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleCommentRepository.findById(id).orElseThrow(NotFoundException::new);
        var articleOptional = articleRepository.findById(comment.getTarget().getId());
        if(articleOptional.isEmpty()){
            log.error("删除评论时，评论存在，但文章不存在\tarticleCommentId=" + comment.getId() + "\tarticleId=" + comment.getTarget().getId());
            throw new SystemException();
        }
        var article = articleOptional.get();

        //检测权限
        if(!comment.getCreatedUser().getId().equals(user.getId()) && !article.getCreatedUser().getId().equals(user.getId())){
            throw new ForbiddenException();
        }

        articleCommentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleAgree(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleCommentRepository.findById(id).orElseThrow(NotFoundException::new);
        synchronized (SynchronizedPrefixConstant.TOGGLE_ARTICLE_COMMENT_AGREE_AND_TREAD + id){
            var agreeOptional = articleCommentAgreeRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
            var treadOptional = articleCommentTreadRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
            if(agreeOptional.isEmpty()){//如果没有点赞，那么点赞，并取消踩
                treadOptional.ifPresent(articleCommentTreadRepository::delete);
                var agree = new ArticleCommentAgree();
                agree.setComment(comment);
                articleCommentAgreeRepository.save(agree);
            }else{
                agreeOptional.ifPresent(articleCommentAgreeRepository::delete);
                var tread = new ArticleCommentTread();
                tread.setComment(comment);
                articleCommentTreadRepository.save(tread);
            }

            //更新点赞数和踩数量
            var agreeNum = articleCommentAgreeRepository.countByCommentId(id);
            var treadNum = articleCommentTreadRepository.countByCommentId(id);
            comment.setAgreedNum(agreeNum);
            comment.setTreadNum(treadNum);
            articleCommentRepository.save(comment);
            return new ValueVo<>(agreeOptional.isEmpty());
        }
    }

    @Override
    public ValueVo<Boolean> toggleTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleCommentRepository.findById(id).orElseThrow(NotFoundException::new);
        synchronized (SynchronizedPrefixConstant.TOGGLE_ARTICLE_COMMENT_AGREE_AND_TREAD + id){
            var treadOptional = articleCommentTreadRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
            var agreeOptional = articleCommentAgreeRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
            if(treadOptional.isEmpty()){//如果没有点踩，那么点踩，并取消赞
                agreeOptional.ifPresent(articleCommentAgreeRepository::delete);
                var tread = new ArticleCommentTread();
                tread.setComment(comment);
                articleCommentTreadRepository.save(tread);
            }else{
                treadOptional.ifPresent(articleCommentTreadRepository::delete);
                var agree = new ArticleCommentAgree();
                agree.setComment(comment);
                articleCommentAgreeRepository.save(agree);
            }

            //更新点赞数和踩数量
            var agreeNum = articleCommentAgreeRepository.countByCommentId(id);
            var treadNum = articleCommentTreadRepository.countByCommentId(id);
            comment.setAgreedNum(agreeNum);
            comment.setTreadNum(treadNum);
            articleCommentRepository.save(comment);
            return new ValueVo<>(agreeOptional.isEmpty());
        }
    }

    @Override
    public Page<ArticleCommentListVo> getPage(Long articleId, Pageable pageable) {
        var page = articleCommentRepository.findAllByTargetId(articleId, pageable);
        var contentIdList = page.getContent().stream().map(ArticleComment::getId).collect(Collectors.toList());
        //子评论
        var replies = articleSubCommentRepository.findAllByRecommendIsTrueAndTargetIn(page.getContent());
        //子评论数量统计
        var repliesTotalMap = articleCommentRepository
                .getRepliesTotals(contentIdList)
                .stream()
                .collect(Collectors.toUnmodifiableMap(RepliesTotalVo::getCommentId, RepliesTotalVo::getCount));
        var voPage = page.map(comment->{
            var vo = new ArticleCommentListVo();
            vo.setId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setAgreeNum(comment.getAgreedNum());
            vo.setTreadNum(comment.getTreadNum());
            vo.setCreatedUser(new BaseUserVo(comment.getCreatedUser()));
            vo.setCreatedDate(comment.getCreatedDate());
            vo.setReplies(replies.stream().filter(i->i.getTarget().getId().equals(comment.getId())).map(ArticleCommentVo::new).collect(Collectors.toList()));//子评论
            vo.setRepliesNum(repliesTotalMap.get(comment.getId()));//子评论数量
            return vo;
        });
        //查询当前用户是否点赞和点踩
        var userOptional = userAuditingListener.getCurrentAuditor();
        if(userOptional.isPresent()){
            var user = userOptional.get();
            var articleCommentIsAgreedSet = articleCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), contentIdList)
                    .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            var articleCommentIsTrodSet = articleCommentTreadRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), contentIdList)
                    .stream().map(t->t.getComment().getId()).collect(Collectors.toSet());
            var articleSubCommentIsAgreedSet = articleSubCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), contentIdList)
                    .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            var articleSubCommentIsTrodSet = articleSubCommentTreadRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), contentIdList)
                    .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            voPage.forEach(v->{
                v.setIsAgreed(articleCommentIsAgreedSet.contains(v.getId()));
                v.setIsTrod(articleCommentIsTrodSet.contains(v.getId()));
                v.getReplies().forEach(c->{
                    c.setIsAgreed(articleSubCommentIsAgreedSet.contains(c.getId()));
                    c.setIsTrod(articleSubCommentIsTrodSet.contains(c.getId()));
                });
            });
        }
        return voPage;
    }
}
