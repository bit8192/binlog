package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.dto.LeftMessageDto;
import cn.bincker.web.blog.base.entity.*;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.service.ILeftMessageService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.LeftMessageVo;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.utils.CommonUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class LeftMessageServiceImpl implements ILeftMessageService {
    private final ILeftMessageAgreeRepository leftMessageAgreeRepository;
    private final ILeftMessageReplyAgreeRepository leftMessageReplyAgreeRepository;
    private final ILeftMessageReplyRepository leftMessageReplyRepository;
    private final ILeftMessageReplyTreadRepository leftMessageReplyTreadRepository;
    private final ILeftMessageRepository leftMessageRepository;
    private final ILeftMessageTreadRepository leftMessageTreadRepository;
    private final UserAuditingListener userAuditingListener;

    public LeftMessageServiceImpl(ILeftMessageAgreeRepository leftMessageAgreeRepository, ILeftMessageReplyAgreeRepository leftMessageReplyAgreeRepository, ILeftMessageReplyRepository leftMessageReplyRepository, ILeftMessageReplyTreadRepository leftMessageReplyTreadRepository, ILeftMessageRepository leftMessageRepository, ILeftMessageTreadRepository leftMessageTreadRepository, UserAuditingListener userAuditingListener) {
        this.leftMessageAgreeRepository = leftMessageAgreeRepository;
        this.leftMessageReplyAgreeRepository = leftMessageReplyAgreeRepository;
        this.leftMessageReplyRepository = leftMessageReplyRepository;
        this.leftMessageReplyTreadRepository = leftMessageReplyTreadRepository;
        this.leftMessageRepository = leftMessageRepository;
        this.leftMessageTreadRepository = leftMessageTreadRepository;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public Page<LeftMessageVo> getPage(Pageable pageable) {
        var currentUserOptional = userAuditingListener.getCurrentAuditor();
        var page = leftMessageRepository.findAll(pageable).map(LeftMessageVo::new);
        var leftMessageMap = page.getContent().stream().collect(Collectors.toUnmodifiableMap(LeftMessageVo::getId, m->m));
        var leftMessageIds = page.getContent().stream().map(LeftMessageVo::getId).collect(Collectors.toSet());

        //处理回复消息
        var recommendReplies = leftMessageReplyRepository.findAllByRecommendIsTrueAndCommentIdIn(leftMessageIds);
        for (LeftMessageReply recommendReply : recommendReplies) {
            var leftMessage = leftMessageMap.get(recommendReply.getComment().getId());
            var replies = leftMessage.getReplies();
            if(replies == null){
                replies = new ArrayList<>();
                leftMessage.setReplies(replies);
            }
            replies.add(new LeftMessageVo(recommendReply));
        }

//        处理匿名和删除
        if(currentUserOptional.isEmpty() || !CommonUtils.checkRoleIn(currentUserOptional.get().getRoles(), Role.RoleEnum.ADMIN, Role.RoleEnum.BLOGGER)){
            page.forEach(msg->{
                if(msg.getIsAnonymous()) msg.setCreatedUser(null);
                if(msg.getRemoved()) msg.setContent("");

                var replies = msg.getReplies();
                if(replies == null) return;
                replies.forEach(reply->{
                    if(reply.getIsAnonymous()) reply.setCreatedUser(null);
                    if(reply.getRemoved()) reply.setContent("");
                });
            });
        }

//        处理点赞
        if(currentUserOptional.isPresent()){
            var user = currentUserOptional.get();
            var agreedLeftMessageIds = leftMessageAgreeRepository.findAllByCreatedUserAndMessageIdIn(user, leftMessageIds).stream().map(a->a.getMessage().getId()).collect(Collectors.toSet());
            var trodLeftMessageIds = leftMessageTreadRepository.findAllByCreatedUserAndMessageIdIn(user, leftMessageIds).stream().map(a->a.getMessage().getId()).collect(Collectors.toSet());

            var recommendRepliesIds = recommendReplies.stream().map(LeftMessageReply::getId).collect(Collectors.toSet());
            var agreedLeftMessageReplyIds = leftMessageReplyAgreeRepository.findAllByCreatedUserAndMessageIdIn(user, recommendRepliesIds).stream().map(a->a.getMessage().getId()).collect(Collectors.toSet());
            var trodLeftMessageReplyIds = leftMessageReplyTreadRepository.findAllByCreatedUserAndMessageIdIn(user, recommendRepliesIds).stream().map(a->a.getMessage().getId()).collect(Collectors.toSet());

            for (LeftMessageVo leftMessageVo : page) {
                leftMessageVo.setIsAgreed(agreedLeftMessageIds.contains(leftMessageVo.getId()));
                leftMessageVo.setIsTrod(trodLeftMessageIds.contains(leftMessageVo.getId()));
                var replies = leftMessageVo.getReplies();
                if(replies != null){
                    for (LeftMessageVo reply : replies) {
                        reply.setIsAgreed(agreedLeftMessageReplyIds.contains(reply.getId()));
                        reply.setIsTrod(trodLeftMessageReplyIds.contains(reply.getId()));
                    }
                }
            }
        }

        return page;
    }

    @Override
    public Page<LeftMessageVo> getReplyPage(Long leftMessageId, Pageable pageable) {
        var page = leftMessageReplyRepository.findAllByCommentId(leftMessageId, pageable).map(LeftMessageVo::new);
        //处理点赞
        var userOptional = userAuditingListener.getCurrentAuditor();
        if(userOptional.isPresent()){
            var user = userOptional.get();
            var replyIds = page.getContent().stream().map(LeftMessageVo::getId).collect(Collectors.toSet());
            var agreedIds = leftMessageReplyAgreeRepository.findAllByCreatedUserAndMessageIdIn(user, replyIds).stream().map(m->m.getMessage().getId()).collect(Collectors.toSet());
            var trodIds = leftMessageReplyTreadRepository.findAllByCreatedUserAndMessageIdIn(user, replyIds).stream().map(m->m.getMessage().getId()).collect(Collectors.toSet());
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

        return page;
    }

    @Override
    public LeftMessageVo leavingMessage(LeftMessageDto dto) {
        var leftMessage = new LeftMessage();
        leftMessage.setContent(dto.getContent());
        leftMessage.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous());
        leftMessage.setAgreedNum(0L);
        leftMessage.setTreadNum(0L);
        leftMessage.setRemoved(false);
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var vo = new LeftMessageVo(leftMessageRepository.save(leftMessage));
        vo.setCreatedUser(new BaseUserVo(user));
        return vo;
    }

    @Override
    public LeftMessageVo replyLeftMessage(Long msgId, LeftMessageDto dto) {
        var parent = leftMessageRepository.findById(msgId).orElseThrow(NotFoundException::new);
        var replyNum = leftMessageReplyRepository.countByComment(parent);
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var reply = new LeftMessageReply();
        reply.setComment(parent);
        reply.setRecommend(replyNum < 5);
        reply.setComment(parent);
        reply.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous());
        reply.setAgreedNum(0L);
        reply.setTreadNum(0L);
        reply.setRemoved(false);
        var vo = new LeftMessageVo(leftMessageReplyRepository.save(reply));
        vo.setCreatedUser(new UserDetailVo(currentUser));
        return vo;
    }

    @Override
    public void removeLeftMessage(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = leftMessageRepository.findById(id).orElseThrow(NotFoundException::new);
        if(!user.getId().equals(msg.getCreatedUser().getId()) && CommonUtils.checkRoleIn(user.getRoles(), Role.RoleEnum.ADMIN))
            throw new ForbiddenException();
        msg.setRemoved(true);
        leftMessageRepository.save(msg);
    }

    @Override
    public void removeLeftMessageReply(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = leftMessageReplyRepository.findById(id).orElseThrow(NotFoundException::new);
        if(!user.getId().equals(msg.getCreatedUser().getId()) && CommonUtils.checkRoleIn(user.getRoles(), Role.RoleEnum.ADMIN))
            throw new ForbiddenException();
        msg.setRemoved(true);
        leftMessageReplyRepository.save(msg);
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleLeftMessageAgree(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = leftMessageRepository.findById(id).orElseThrow(NotFoundException::new);
        var agreeOptional = leftMessageAgreeRepository.findByCreatedUserAndMessageId(user, id);
        if(agreeOptional.isEmpty()){
            var agree = new LeftMessageAgree();
            agree.setMessage(msg);
            leftMessageAgreeRepository.save(agree);
            var treadOptional = leftMessageTreadRepository.findByCreatedUserAndMessageId(user, id);
            treadOptional.ifPresent(leftMessageTreadRepository::delete);
        }else{
            leftMessageAgreeRepository.delete(agreeOptional.get());
        }
        this.updateLeftMessageAgreedAndTreadNum(id);
        return new ValueVo<>(agreeOptional.isEmpty());
    }

    private void updateLeftMessageAgreedAndTreadNum(Long id){
        synchronized ((SynchronizedPrefixConstant.TOGGLE_LEFT_MESSAGE_COUNT_AGREE_AND_TREAD + id).intern()){
            var msg = leftMessageRepository.getOne(id);
            msg.setAgreedNum(leftMessageAgreeRepository.countByMessageId(id));
            msg.setTreadNum(leftMessageTreadRepository.countByMessageId(id));
            leftMessageRepository.save(msg);
        }
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleLeftMessageReplyAgree(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = leftMessageReplyRepository.findById(id).orElseThrow(NotFoundException::new);
        var agreeOptional = leftMessageReplyAgreeRepository.findByCreatedUserAndMessageId(user, id);
        if(agreeOptional.isEmpty()){
            var agree = new LeftMessageReplyAgree();
            agree.setMessage(msg);
            leftMessageReplyAgreeRepository.save(agree);
            var treadOptional = leftMessageReplyTreadRepository.findByCreatedUserAndMessageId(user, id);
            treadOptional.ifPresent(leftMessageReplyTreadRepository::delete);
        }else{
            leftMessageReplyAgreeRepository.delete(agreeOptional.get());
        }
        this.updateLeftMessageReplyAgreedAndTreadNum(id);
        return new ValueVo<>(agreeOptional.isEmpty());
    }

    private void updateLeftMessageReplyAgreedAndTreadNum(Long id){
        synchronized ((SynchronizedPrefixConstant.TOGGLE_LEFT_MESSAGE_REPLY_COUNT_AGREE_AND_TREAD + id).intern()){
            var msg = leftMessageReplyRepository.getOne(id);
            msg.setAgreedNum(leftMessageReplyAgreeRepository.countByMessageId(id));
            msg.setTreadNum(leftMessageReplyTreadRepository.countByMessageId(id));
            leftMessageReplyRepository.save(msg);
        }
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleLeftMessageTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = leftMessageRepository.findById(id).orElseThrow(NotFoundException::new);
        var treadOptional = leftMessageTreadRepository.findByCreatedUserAndMessageId(user, id);
        if(treadOptional.isEmpty()){
            var tread = new LeftMessageTread();
            tread.setMessage(msg);
            leftMessageTreadRepository.save(tread);
            var agreeOptional = leftMessageAgreeRepository.findByCreatedUserAndMessageId(user, id);
            agreeOptional.ifPresent(leftMessageAgreeRepository::delete);
        }else{
            leftMessageTreadRepository.delete(treadOptional.get());
        }
        this.updateLeftMessageAgreedAndTreadNum(id);
        return new ValueVo<>(treadOptional.isEmpty());
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleLeftMessageReplyTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var msg = leftMessageReplyRepository.findById(id).orElseThrow(NotFoundException::new);
        var treadOptional = leftMessageReplyTreadRepository.findByCreatedUserAndMessageId(user, id);
        if(treadOptional.isEmpty()){
            var tread = new LeftMessageReplyTread();
            tread.setMessage(msg);
            leftMessageReplyTreadRepository.save(tread);
            var AgreeOptional = leftMessageReplyAgreeRepository.findByCreatedUserAndMessageId(user, id);
            AgreeOptional.ifPresent(leftMessageReplyAgreeRepository::delete);
        }else{
            leftMessageReplyTreadRepository.delete(treadOptional.get());
        }
        this.updateLeftMessageReplyAgreedAndTreadNum(id);
        return new ValueVo<>(treadOptional.isEmpty());
    }
}
