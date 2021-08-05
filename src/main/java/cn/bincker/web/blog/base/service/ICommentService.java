package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.entity.*;
import cn.bincker.web.blog.base.event.MessageEvent;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.vo.*;
import cn.bincker.web.blog.utils.CommonUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

public interface ICommentService {

    /**
     * 获取回复分页
     */
    Page<CommentVo> getReplyPage(Long commentId, Pageable pageable);

    /**
     * 评论
     */
    static Comment commenting(ICommentRepository commentRepository, Comment.Type type, CommentDto dto) {
        var comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous());
        comment.setAgreedNum(0L);
        comment.setTreadNum(0L);
        comment.setRemoved(false);
        comment.setType(type);
        return commentRepository.save(comment);
    }


    /**
     * 回复评论
     */
    static CommentVo replyComment(ICommentReplyRepository commentReplyRepository, IBaseUserRepository userRepository, BaseUser currentUser, Comment comment, CommentDto dto) {
        if(comment.getRemoved()) throw new ForbiddenException();
        var replyNum = commentReplyRepository.countByComment(comment);
        var currentReply = new CommentReply();
        currentReply.setContent(dto.getContent());
        currentReply.setComment(comment);
        currentReply.setRecommend(replyNum < 5);
        currentReply.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous());
        currentReply.setAgreedNum(0L);
        currentReply.setTreadNum(0L);
        currentReply.setRemoved(false);
        var vo = new CommentVo(commentReplyRepository.save(currentReply));
        vo.setCreatedUser(new UserDetailVo(currentUser));
        handleMember(userRepository, Collections.singletonList(vo));
        return vo;
    }

    /**
     * 回复子评论
     */
    static CommentVo replySubComment(ICommentReplyRepository commentReplyRepository, IBaseUserRepository userRepository, BaseUser currentUser, Comment comment, CommentReply reply, CommentDto dto) {
        if(reply.getRemoved()) throw new ForbiddenException();
        var replyNum = commentReplyRepository.countByComment(comment);
        var currentReply = new CommentReply();
        currentReply.setContent("回复 @" + reply.getCreatedUser().getUsername() + " : " + dto.getContent());
        currentReply.setComment(comment);
        currentReply.setRecommend(replyNum < 5);
        currentReply.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous());
        currentReply.setAgreedNum(0L);
        currentReply.setTreadNum(0L);
        currentReply.setRemoved(false);
        var vo = new CommentVo(commentReplyRepository.save(currentReply));
        vo.setCreatedUser(new UserDetailVo(currentUser));
        handleMember(userRepository, Collections.singletonList(vo));
        return vo;
    }

    /**
     * 删除留言
     */
    void removeComment(Long id);

    /**
     * 删除留言评论
     */
    void removeReply(Long id);

    /**
     * 切换留言点赞
     */
    ValueVo<Boolean> toggleCommentAgree(Long id);

    /**
     * 切换留言评论点赞
     */
    ValueVo<Boolean> toggleReplyAgree(Long id);

    /**
     * 切换留言点踩
     */
    ValueVo<Boolean> toggleCommentTread(Long id);

    /**
     * 切换评论留言点踩
     */
    ValueVo<Boolean> toggleReplyTread(Long id);


    /**
     * 处理评论的业务代码
     */
    static Page<CommentVo> handleComment(ICommentReplyRepository commentReplyRepository, ICommentAgreeRepository commentAgreeRepository, ICommentTreadRepository commentTreadRepository, ICommentReplyAgreeRepository commentReplyAgreeRepository, ICommentReplyTreadRepository commentReplyTreadRepository, IBaseUserRepository userRepository, Optional<BaseUser> userOptional, Page<Comment> page) {
        var voPage = page.map(CommentVo::new);
        var commentMap = voPage.getContent().stream().collect(Collectors.toUnmodifiableMap(CommentVo::getId, m->m));
        var commentIds = voPage.getContent().stream().map(CommentVo::getId).collect(Collectors.toSet());

//        处理回复消息
        var recommendReplies = commentReplyRepository.findAllByRecommendIsTrueAndCommentIdIn(commentIds);
        for (CommentReply recommendReply : recommendReplies) {
            var comment = commentMap.get(recommendReply.getComment().getId());
            var replies = comment.getReplies();
            if(replies == null){
                replies = new ArrayList<>();
                comment.setReplies(replies);
            }
            replies.add(new CommentVo(recommendReply));
        }

//        处理@的用户
        ICommentService.handleMember(userRepository, voPage);
        ICommentService.handleMember(userRepository, voPage.getContent().stream().map(CommentVo::getReplies).filter(Objects::nonNull).reduce(new ArrayList<>(), (r, i)->{r.addAll(i); return r;}));

//        处理回复消息数量
        var commentRepliesNumMap = commentReplyRepository.countAllByCommentIds(commentIds).stream().collect(Collectors.toUnmodifiableMap(EntityLongValueVo::getId, EntityLongValueVo::getValue));
        for (CommentVo commentVo : voPage) {
            commentVo.setRepliesNum(commentRepliesNumMap.get(commentVo.getId()));
        }

//        处理匿名和删除
        if(userOptional.isEmpty() || !CommonUtils.checkRoleIn(userOptional.get().getRoles(), Role.RoleEnum.ADMIN, Role.RoleEnum.BLOGGER)){
            voPage.forEach(msg->{
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
        if(userOptional.isPresent()){
            var user = userOptional.get();
            var agreedLeftMessageIds = commentAgreeRepository.findAllByCreatedUserAndCommentIdIn(user, commentIds).stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            var trodLeftMessageIds = commentTreadRepository.findAllByCreatedUserAndCommentIdIn(user, commentIds).stream().map(a->a.getComment().getId()).collect(Collectors.toSet());

            var recommendRepliesIds = recommendReplies.stream().map(CommentReply::getId).collect(Collectors.toSet());
            var agreedLeftMessageReplyIds = commentReplyAgreeRepository.findAllByCreatedUserAndCommentIdIn(user, recommendRepliesIds).stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            var trodLeftMessageReplyIds = commentReplyTreadRepository.findAllByCreatedUserAndCommentIdIn(user, recommendRepliesIds).stream().map(a->a.getComment().getId()).collect(Collectors.toSet());

            for (CommentVo commentVo : voPage) {
                commentVo.setIsAgreed(agreedLeftMessageIds.contains(commentVo.getId()));
                commentVo.setIsTrod(trodLeftMessageIds.contains(commentVo.getId()));
                var replies = commentVo.getReplies();
                if(replies != null){
                    for (CommentVo reply : replies) {
                        reply.setIsAgreed(agreedLeftMessageReplyIds.contains(reply.getId()));
                        reply.setIsTrod(trodLeftMessageReplyIds.contains(reply.getId()));
                    }
                }
            }
        }

        return voPage;
    }

    /**
     * 处理@到的用户
     */
    static void handleMember(IBaseUserRepository userRepository, Iterable<CommentVo> commentVos){
        var userNameList = new ArrayList<String>();
        var commentMemberNamesMap = new HashMap<Long, Set<String>>();
        for (var comment : commentVos) {
            var matcher = RegexpConstant.COMMENT_MEMBER.matcher(comment.getContent());
            var memberNames = commentMemberNamesMap.computeIfAbsent(comment.getId(), k->new HashSet<>());
            while (matcher.find()) {
                var username = matcher.group(1);
                userNameList.add(username);
                memberNames.add(username);
            }
        }
        var userList = userNameList.isEmpty() ? Collections.<BaseUser>emptyList() : userRepository.findAllByUsernameIn(userNameList);
        var userNameMap = userList.stream().collect(Collectors.toUnmodifiableMap(BaseUser::getUsername, u->u));
        for (CommentVo commentVo : commentVos) {
            commentVo.setMembers(
                    commentMemberNamesMap.get(commentVo.getId()).stream()
                            .filter(userNameMap::containsKey)
                            .map(username->new BaseUserVo(userNameMap.get(username)))
                            .collect(Collectors.toSet())
            );
        }
    }

    /**
     * 发送@到的用户提醒
     */
    static void sendMentionUserMessage(
            ApplicationContext applicationContext,
            IBaseUserRepository userRepository,
            Message.Type type,
            BaseUser currentUser,
            CommentVo reply,
            Long relatedTargetId,
            Long originalTargetId
    ) {
        var pattern = RegexpConstant.REPLY_CONTENT_PREFIX.matcher(reply.getContent());
        var mentionUsers = reply.getMembers();
        if(pattern.find()){
            var username = pattern.group(1);
            mentionUsers = mentionUsers.stream().filter(u->!u.getUsername().equals(username)).collect(Collectors.toSet());
        }
        for (BaseUserVo mentionUser : mentionUsers) {
            var event = new MessageEvent(
                    applicationContext,
                    null,
                    type,
                    currentUser,
                    userRepository.getOne(mentionUser.getId()),
                    relatedTargetId,
                    originalTargetId,
                    reply.getId()
            );
            applicationContext.publishEvent(event);
        }
    }

    /**
     * 回复评论
     */
    CommentVo replyComment(Long commentId, CommentDto dto);

    /**
     * 回复子评论
     */
    CommentVo replySubComment(Long replyId, CommentDto dto);
}
