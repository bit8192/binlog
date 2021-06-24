package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import cn.bincker.web.blog.material.entity.ArticleSubCommentAgree;
import cn.bincker.web.blog.material.entity.ArticleSubCommentTread;
import cn.bincker.web.blog.material.repository.IArticleCommentRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentAgreeRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentTreadRepository;
import cn.bincker.web.blog.material.service.IArticleSubCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class ArticleSubCommentServiceImpl implements IArticleSubCommentService {
    private static final Logger log = LoggerFactory.getLogger(ArticleSubCommentServiceImpl.class);

    private final IArticleSubCommentRepository articleSubCommentRepository;
    private final IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository;
    private final IArticleSubCommentTreadRepository articleSubCommentTreadRepository;
    private final UserAuditingListener userAuditingListener;
    private final IArticleCommentRepository articleCommentRepository;

    public ArticleSubCommentServiceImpl(IArticleSubCommentRepository articleSubCommentRepository, IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository, IArticleSubCommentTreadRepository articleSubCommentTreadRepository, UserAuditingListener userAuditingListener, IArticleCommentRepository articleCommentRepository) {
        this.articleSubCommentRepository = articleSubCommentRepository;
        this.articleSubCommentAgreeRepository = articleSubCommentAgreeRepository;
        this.articleSubCommentTreadRepository = articleSubCommentTreadRepository;
        this.userAuditingListener = userAuditingListener;
        this.articleCommentRepository = articleCommentRepository;
    }

    @Override
    public ArticleCommentVo comment(Long articleCommentId, ArticleCommentDto dto) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var articleComment = articleCommentRepository.findById(articleCommentId).orElseThrow(NotFoundException::new);
        var subCommentTotal = articleSubCommentRepository.countByTarget(articleComment);
        var comment = new ArticleSubComment();
        comment.setContent(dto.getContent());
        comment.setTarget(articleComment);
        comment.setCreatedUser(user);
        comment.setRecommend(subCommentTotal < 3);
        articleSubCommentRepository.save(comment);
        return new ArticleCommentVo(comment);
    }

    @Override
    public void del(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleSubCommentRepository.findById(id).orElseThrow(NotFoundException::new);
        var articleCommentOptional = articleCommentRepository.findById(comment.getTarget().getId());
        if(articleCommentOptional.isEmpty()){
            log.error("删除子评论时上级评论为空：subCommentId=" + comment.getId() + "\tarticleCommentId=" + comment.getTarget().getId());
            throw new SystemException();
        }
        var articleComment = articleCommentOptional.get();
        if(!articleComment.getCreatedUser().getId().equals(user.getId()) && !comment.getCreatedUser().getId().equals(user.getId()))
            throw new ForbiddenException();
        articleSubCommentRepository.delete(comment);
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleAgree(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleSubCommentRepository.findById(id).orElseThrow(NotFoundException::new);
        synchronized (SynchronizedPrefixConstant.TOGGLE_ARTICLE_SUB_COMMENT_AGREE_AND_TREAD + id){
            var agreeOptional = articleSubCommentAgreeRepository.findByCreatedUserAndComment(user, comment);
            var treadOptional = articleSubCommentTreadRepository.findByCreatedUserAndComment(user, comment);
            if(agreeOptional.isEmpty()){
                var agree = new ArticleSubCommentAgree();
                agree.setComment(comment);
                articleSubCommentAgreeRepository.save(agree);
                treadOptional.ifPresent(articleSubCommentTreadRepository::delete);
            }else{
                var tread = new ArticleSubCommentTread();
                tread.setComment(comment);
                articleSubCommentTreadRepository.save(tread);
                agreeOptional.ifPresent(articleSubCommentAgreeRepository::delete);
            }

            //重新统计数量
            var agreeNum = articleSubCommentAgreeRepository.countByComment(comment);
            var treadNum = articleSubCommentTreadRepository.countByComment(comment);
            comment.setAgreedNum(agreeNum);
            comment.setTreadNum(treadNum);
            articleSubCommentRepository.save(comment);

            return new ValueVo<>(agreeOptional.isEmpty());
        }
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleSubCommentRepository.findById(id).orElseThrow(NotFoundException::new);
        synchronized (SynchronizedPrefixConstant.TOGGLE_ARTICLE_SUB_COMMENT_AGREE_AND_TREAD + id){
            var treadOptional = articleSubCommentTreadRepository.findByCreatedUserAndComment(user, comment);
            var agreeOptional = articleSubCommentAgreeRepository.findByCreatedUserAndComment(user, comment);
            if(treadOptional.isEmpty()){
                var tread = new ArticleSubCommentTread();
                tread.setComment(comment);
                articleSubCommentTreadRepository.save(tread);
                agreeOptional.ifPresent(articleSubCommentAgreeRepository::delete);
            }else{
                var agree = new ArticleSubCommentAgree();
                agree.setComment(comment);
                articleSubCommentAgreeRepository.save(agree);
                treadOptional.ifPresent(articleSubCommentTreadRepository::delete);
            }

            //重新统计数量
            var agreeNum = articleSubCommentAgreeRepository.countByComment(comment);
            var treadNum = articleSubCommentTreadRepository.countByComment(comment);
            comment.setAgreedNum(agreeNum);
            comment.setTreadNum(treadNum);
            articleSubCommentRepository.save(comment);

            return new ValueVo<>(agreeOptional.isEmpty());
        }
    }

    @Override
    public Page<ArticleCommentVo> getPage(Long articleCommentId, Pageable pageable) {
        var articleComment = articleCommentRepository.findById(articleCommentId).orElseThrow(NotFoundException::new);
        var page = articleSubCommentRepository.findAllByTarget(articleComment, pageable);
        var userOptional = userAuditingListener.getCurrentAuditor();
        var voPage = page.map(ArticleCommentVo::new);
        if(userOptional.isPresent()){
            var user = userOptional.get();
            var commentIdList = page.getContent().stream().map(ArticleSubComment::getId).collect(Collectors.toList());
            var isAgreedCommentIdSet = articleSubCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), commentIdList)
                    .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            var isTrodCommentIdSet = articleSubCommentTreadRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), commentIdList)
                    .stream().map(t->t.getComment().getId()).collect(Collectors.toSet());
            voPage.forEach(v->{
                v.setIsAgreed(isAgreedCommentIdSet.contains(v.getId()));
                v.setIsTrod(isTrodCommentIdSet.contains(v.getId()));
            });
        }
        return voPage;
    }
}
