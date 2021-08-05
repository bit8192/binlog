package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.entity.*;
import cn.bincker.web.blog.base.event.MessageEvent;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.service.ICommentService;
import cn.bincker.web.blog.base.vo.*;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.utils.CommonUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements ICommentService {
    private final ICommentAgreeRepository commentAgreeRepository;
    private final ICommentReplyAgreeRepository commentReplyAgreeRepository;
    private final ICommentReplyRepository commentReplyRepository;
    private final ICommentReplyTreadRepository commentReplyTreadRepository;
    private final ICommentRepository commentRepository;
    private final ICommentTreadRepository commentTreadRepository;
    private final UserAuditingListener userAuditingListener;
    private final IBaseUserRepository userRepository;
    private final IArticleRepository articleRepository;
    private final ApplicationContext applicationContext;

    public CommentServiceImpl(ICommentAgreeRepository commentAgreeRepository, ICommentReplyAgreeRepository commentReplyAgreeRepository, ICommentReplyRepository commentReplyRepository, ICommentReplyTreadRepository commentReplyTreadRepository, ICommentRepository commentRepository, ICommentTreadRepository commentTreadRepository, UserAuditingListener userAuditingListener, IBaseUserRepository userRepository, IArticleRepository articleRepository, ApplicationContext applicationContext) {
        this.commentAgreeRepository = commentAgreeRepository;
        this.commentReplyAgreeRepository = commentReplyAgreeRepository;
        this.commentReplyRepository = commentReplyRepository;
        this.commentReplyTreadRepository = commentReplyTreadRepository;
        this.commentRepository = commentRepository;
        this.commentTreadRepository = commentTreadRepository;
        this.userAuditingListener = userAuditingListener;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public Page<CommentVo> getReplyPage(Long commentId, Pageable pageable) {
        var page = commentReplyRepository.findAllByCommentId(commentId, pageable).map(CommentVo::new);
        //处理点赞
        var userOptional = userAuditingListener.getCurrentAuditor();
        if(userOptional.isPresent()){
            var user = userOptional.get();
            var replyIds = page.getContent().stream().map(CommentVo::getId).collect(Collectors.toSet());
            var agreedIds = commentReplyAgreeRepository.findAllByCreatedUserAndCommentIdIn(user, replyIds).stream().map(m->m.getComment().getId()).collect(Collectors.toSet());
            var trodIds = commentReplyTreadRepository.findAllByCreatedUserAndCommentIdIn(user, replyIds).stream().map(m->m.getComment().getId()).collect(Collectors.toSet());
            page.forEach(m->{
                m.setIsAgreed(agreedIds.contains(m.getId()));
                m.setIsTrod(trodIds.contains(m.getId()));
            });
        }

//        处理匿名和删除
        if(userOptional.isEmpty() || !CommonUtils.checkRoleIn(userOptional.get().getRoles(), Role.RoleEnum.ADMIN, Role.RoleEnum.BLOGGER)){
            page.forEach(m->{
                if(m.getIsAnonymous()) m.setCreatedUser(null);
                if(m.getRemoved()) m.setContent("");
            });
        }

//        处理@到的用户
        ICommentService.handleMember(userRepository, page);
        return page;
    }

    @Override
    public void removeComment(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = commentRepository.findById(id).orElseThrow(NotFoundException::new);
        if(!user.getId().equals(msg.getCreatedUser().getId()) && CommonUtils.checkRoleIn(user.getRoles(), Role.RoleEnum.ADMIN))
            throw new ForbiddenException();
        msg.setRemoved(true);
        commentRepository.save(msg);
    }

    @Override
    public void removeReply(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = commentReplyRepository.findById(id).orElseThrow(NotFoundException::new);
        if(!user.getId().equals(msg.getCreatedUser().getId()) && CommonUtils.checkRoleIn(user.getRoles(), Role.RoleEnum.ADMIN))
            throw new ForbiddenException();
        msg.setRemoved(true);
        commentReplyRepository.save(msg);
    }

    @Override
    public CommentVo replyComment(Long commentId, CommentDto dto) {
        var comment = commentRepository.findById(commentId).orElseThrow(NotFoundException::new);
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var reply = ICommentService.replyComment(commentReplyRepository, userRepository, currentUser, comment, dto);

        Long relatedTargetId = null;//相关对象ID
        Message.Type triggerReplyMessageType = null;//触发消息事件的消息类型
        Message.Type triggerMentionMessageType = null;//触发@用户事件的消息类型
        switch (comment.getType()) {
            case ARTICLE -> {
                var article = articleRepository.findByCommentId(commentId).orElseThrow(NotFoundException::new);
                relatedTargetId = article.getId();
                triggerReplyMessageType = Message.Type.ARTICLE_COMMENT_REPLY;
                triggerMentionMessageType = Message.Type.ARTICLE_COMMENT_MENTION;
                dto.setIsAnonymous(false);
            }
        }
//        触发消息
        if(!currentUser.getId().equals(comment.getCreatedUser().getId())) {
            var messageEvent = new MessageEvent(
                    applicationContext,
                    dto.getContent(),
                    triggerReplyMessageType,
                    currentUser,
                    comment.getCreatedUser(),
                    relatedTargetId,
                    commentId,
                    reply.getId()
            );
            applicationContext.publishEvent(messageEvent);
        }
        ICommentService.sendMentionUserMessage(applicationContext, userRepository, triggerMentionMessageType, currentUser, reply, relatedTargetId, commentId);
        return reply;
    }

    @Override
    public CommentVo replySubComment(Long replyId, CommentDto dto) {
        var reply = commentReplyRepository.findById(replyId).orElseThrow(NotFoundException::new);
        var comment = commentRepository.findById(reply.getComment().getId()).orElseThrow(NotFoundException::new);
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var currentReply = ICommentService.replySubComment(commentReplyRepository, userRepository, currentUser, comment, reply, dto);

        Long relatedTargetId = null;//相关对象ID
        Message.Type triggerReplyMessageType = null;//触发消息事件的消息类型
        Message.Type triggerMentionMessageType = null;//触发@用户事件的消息类型
        switch (comment.getType()){
            case ARTICLE -> {
                var article = articleRepository.findByCommentId(comment.getId()).orElseThrow(NotFoundException::new);
                relatedTargetId = article.getId();
                triggerReplyMessageType = Message.Type.ARTICLE_SUB_COMMENT_REPLY;
                triggerMentionMessageType = Message.Type.ARTICLE_SUB_COMMENT_MENTION;
                dto.setIsAnonymous(false);
            }
        }
        if(!currentUser.getId().equals(reply.getCreatedUser().getId())) {
            var messageEvent = new MessageEvent(
                    applicationContext,
                    dto.getContent(),
                    triggerReplyMessageType,
                    currentUser,
                    comment.getCreatedUser(),
                    relatedTargetId,
                    replyId,
                    reply.getId()
            );
            applicationContext.publishEvent(messageEvent);
        }
        ICommentService.sendMentionUserMessage(applicationContext, userRepository, triggerMentionMessageType, currentUser, currentReply, relatedTargetId, replyId);
        return currentReply;
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleCommentAgree(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = commentRepository.findById(id).orElseThrow(NotFoundException::new);
        if(comment.getRemoved()) throw new ForbiddenException();
        var agreeOptional = commentAgreeRepository.findByCreatedUserAndCommentId(user, id);
        if(agreeOptional.isEmpty()){
            var agree = new CommentAgree();
            agree.setComment(comment);
            commentAgreeRepository.save(agree);
            var treadOptional = commentTreadRepository.findByCreatedUserAndCommentId(user, id);
            treadOptional.ifPresent(commentTreadRepository::delete);
//            触发消息
            if(!user.getId().equals(comment.getCreatedUser().getId())) {
                Message.Type messageType = null;
                Long relatedTargetId = null;
                switch (comment.getType()) {
                    case ARTICLE -> {
                        var article = articleRepository.findByCommentId(comment.getId()).orElseThrow(NotFoundException::new);
                        messageType = Message.Type.ARTICLE_COMMENT_AGREE;
                        relatedTargetId = article.getId();
                    }
                }
                var agreeMessageEvent = new MessageEvent(
                        applicationContext,
                        null,
                        messageType,
                        user,
                        comment.getCreatedUser(),
                        relatedTargetId,
                        comment.getId(),
                        agree.getId()
                );
                applicationContext.publishEvent(agreeMessageEvent);
            }
        }else{
            commentAgreeRepository.delete(agreeOptional.get());
        }
        this.updateLeftMessageAgreedAndTreadNum(id);
        return new ValueVo<>(agreeOptional.isEmpty());
    }

    private void updateLeftMessageAgreedAndTreadNum(Long id){
        synchronized ((SynchronizedPrefixConstant.TOGGLE_LEFT_MESSAGE_COUNT_AGREE_AND_TREAD + id).intern()){
            var msg = commentRepository.getOne(id);
            msg.setAgreedNum(commentAgreeRepository.countByCommentId(id));
            msg.setTreadNum(commentTreadRepository.countByCommentId(id));
            commentRepository.save(msg);
        }
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleReplyAgree(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var reply = commentReplyRepository.findById(id).orElseThrow(NotFoundException::new);
        var comment = reply.getComment();
        var agreeOptional = commentReplyAgreeRepository.findByCreatedUserAndCommentId(user, id);
        if(agreeOptional.isEmpty()){
            var agree = new CommentReplyAgree();
            agree.setComment(reply);
            commentReplyAgreeRepository.save(agree);
            var treadOptional = commentReplyTreadRepository.findByCreatedUserAndCommentId(user, id);
            treadOptional.ifPresent(commentReplyTreadRepository::delete);
//            触发消息事件
            if(!user.getId().equals(comment.getCreatedUser().getId())) {
                Message.Type messageType = null;
                Long relatedTargetId = null;
                switch (comment.getType()) {
                    case ARTICLE -> {
                        var article = articleRepository.findByCommentId(comment.getId()).orElseThrow(NotFoundException::new);
                        messageType = Message.Type.ARTICLE_SUB_COMMENT_AGREE;
                        relatedTargetId = article.getId();
                    }
                }
                var messageEvent = new MessageEvent(
                        applicationContext,
                        null,
                        messageType,
                        user,
                        reply.getCreatedUser(),
                        relatedTargetId,
                        reply.getId(),
                        agree.getId()
                );
                applicationContext.publishEvent(messageEvent);
            }
        }else{
            commentReplyAgreeRepository.delete(agreeOptional.get());
        }
        this.updateLeftMessageReplyAgreedAndTreadNum(id);
        return new ValueVo<>(agreeOptional.isEmpty());
    }

    private void updateLeftMessageReplyAgreedAndTreadNum(Long id){
        synchronized ((SynchronizedPrefixConstant.TOGGLE_LEFT_MESSAGE_REPLY_COUNT_AGREE_AND_TREAD + id).intern()){
            var msg = commentReplyRepository.getOne(id);
            msg.setAgreedNum(commentReplyAgreeRepository.countByCommentId(id));
            msg.setTreadNum(commentReplyTreadRepository.countByCommentId(id));
            commentReplyRepository.save(msg);
        }
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleCommentTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = commentRepository.findById(id).orElseThrow(NotFoundException::new);
        var treadOptional = commentTreadRepository.findByCreatedUserAndCommentId(user, id);
        if(treadOptional.isEmpty()){
            var tread = new CommentTread();
            tread.setComment(comment);
            commentTreadRepository.save(tread);
            var agreeOptional = commentAgreeRepository.findByCreatedUserAndCommentId(user, id);
            agreeOptional.ifPresent(commentAgreeRepository::delete);
        }else{
            commentTreadRepository.delete(treadOptional.get());
        }
        this.updateLeftMessageAgreedAndTreadNum(id);
        return new ValueVo<>(treadOptional.isEmpty());
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleReplyTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = commentReplyRepository.findById(id).orElseThrow(NotFoundException::new);
        var treadOptional = commentReplyTreadRepository.findByCreatedUserAndCommentId(user, id);
        if(treadOptional.isEmpty()){
            var tread = new CommentReplyTread();
            tread.setComment(msg);
            commentReplyTreadRepository.save(tread);
            var AgreeOptional = commentReplyAgreeRepository.findByCreatedUserAndCommentId(user, id);
            AgreeOptional.ifPresent(commentReplyAgreeRepository::delete);
        }else{
            commentReplyTreadRepository.delete(treadOptional.get());
        }
        this.updateLeftMessageReplyAgreedAndTreadNum(id);
        return new ValueVo<>(treadOptional.isEmpty());
    }
}
