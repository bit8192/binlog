package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Comment;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.base.event.MessageEvent;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.service.ICommentService;
import cn.bincker.web.blog.base.service.ILeftMessageService;
import cn.bincker.web.blog.base.specification.BaseUserSpecification;
import cn.bincker.web.blog.base.vo.CommentVo;
import cn.bincker.web.blog.utils.CommonUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class LeftMessageServiceImpl implements ILeftMessageService {
    private final UserAuditingListener userAuditingListener;
    private final ICommentRepository commentRepository;
    private final ICommentReplyRepository commentReplyRepository;
    private final ICommentAgreeRepository commentAgreeRepository;
    private final ICommentTreadRepository commentTreadRepository;
    private final ICommentReplyAgreeRepository commentReplyAgreeRepository;
    private final ICommentReplyTreadRepository commentReplyTreadRepository;
    private final IBaseUserRepository userRepository;
    private final ApplicationContext applicationContext;

    public LeftMessageServiceImpl(UserAuditingListener userAuditingListener, ICommentRepository commentRepository, ICommentReplyRepository commentReplyRepository, ICommentAgreeRepository commentAgreeRepository, ICommentTreadRepository commentTreadRepository, ICommentReplyAgreeRepository commentReplyAgreeRepository, ICommentReplyTreadRepository commentReplyTreadRepository, IBaseUserRepository userRepository, ApplicationContext applicationContext) {
        this.userAuditingListener = userAuditingListener;
        this.commentRepository = commentRepository;
        this.commentReplyRepository = commentReplyRepository;
        this.commentAgreeRepository = commentAgreeRepository;
        this.commentTreadRepository = commentTreadRepository;
        this.commentReplyAgreeRepository = commentReplyAgreeRepository;
        this.commentReplyTreadRepository = commentReplyTreadRepository;
        this.userRepository = userRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public Page<CommentVo> getLeftMessagePage(Pageable pageable) {
        var page = commentRepository.findAllByType(Comment.Type.LEFT_MESSAGE, pageable);
        var currentUser = userAuditingListener.getCurrentAuditor();
        return ICommentService.handleComment(
                commentReplyRepository,
                commentAgreeRepository,
                commentTreadRepository,
                commentReplyAgreeRepository,
                commentReplyTreadRepository,
                userRepository,
                currentUser,
                page
        );
    }

    @Override
    @Transactional
    public CommentVo leavingMessage(CommentDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = ICommentService.commenting(commentRepository, Comment.Type.LEFT_MESSAGE, dto);
        var vo = new CommentVo(comment);
        vo.setIsAgreed(false);
        vo.setIsTrod(false);
        if(dto.isAnonymous){
            vo.setCreatedUser(null);
        }
        ICommentService.handleMember(userRepository, Collections.singletonList(vo));
//        触发消息
        if(!CommonUtils.checkRoleIn(currentUser.getRoles(), Role.RoleEnum.ADMIN, Role.RoleEnum.BLOGGER)) {
            var noticeUsers = userRepository.findAll(BaseUserSpecification.role(Role.RoleEnum.BLOGGER));
            for (BaseUser noticeUser : noticeUsers) {
                var messageEvent = new MessageEvent(
                        applicationContext,
                        dto.getContent(),
                        Message.Type.LEFT_MESSAGE,
                        comment.getCreatedUser(),
                        noticeUser,
                        null,
                        null,
                        comment.getId()
                );
                applicationContext.publishEvent(messageEvent);
            }
        }
        return vo;
    }
}
