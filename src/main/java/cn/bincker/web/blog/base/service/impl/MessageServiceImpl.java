package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.dto.MessageDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Comment;
import cn.bincker.web.blog.base.entity.CommentReply;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.service.IMessageService;
import cn.bincker.web.blog.base.specification.MessageSpecification;
import cn.bincker.web.blog.base.vo.*;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements IMessageService {
    private final IMessageRepository messageRepository;
    private final IBaseUserRepository baseUserRepository;
    private final IArticleRepository articleRepository;
    private final ICommentRepository commentRepository;
    private final ICommentReplyRepository commentReplyRepository;
    private final ICommentAgreeRepository commentAgreeRepository;
    private final ICommentReplyAgreeRepository commentReplyAgreeRepository;

    public MessageServiceImpl(IMessageRepository messageRepository, IBaseUserRepository baseUserRepository, IArticleRepository articleRepository, ICommentRepository commentRepository, ICommentReplyRepository commentReplyRepository, ICommentAgreeRepository commentAgreeRepository, ICommentReplyAgreeRepository commentReplyAgreeRepository) {
        this.messageRepository = messageRepository;
        this.baseUserRepository = baseUserRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.commentReplyRepository = commentReplyRepository;
        this.commentAgreeRepository = commentAgreeRepository;
        this.commentReplyAgreeRepository = commentReplyAgreeRepository;
    }

    @Override
    public Map<Message.Type, Long> getUnreadCount(BaseUser baseUser) {
        return messageRepository
                .queryUnreadCount(baseUser.getId())
                .stream()
                .collect(Collectors.toUnmodifiableMap(UnreadMessageCount::getType, UnreadMessageCount::getCount));
    }

    @Override
    public Page<CommentMessageVo> getArticleCommentMessagePage(BaseUser baseUser, Pageable pageable) {
        var result = messageRepository
                .findAll(
                        MessageSpecification.toUser(baseUser).and(MessageSpecification.type(Message.Type.ARTICLE_COMMENT)),
                        pageable
                );
        var commentIds = result.stream().map(Message::getTargetId).collect(Collectors.toSet());
//        获取评论
        var commentMap = commentRepository.findAllById(commentIds).stream().collect(Collectors.toUnmodifiableMap(Comment::getId, c->c));
//        是否点赞
        var isAgreedCommentIds = commentAgreeRepository.findAllByCreatedUserAndCommentIdIn(baseUser, commentIds)
                .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
//        文章
        var articleMap = articleRepository.findAllById(result.getContent().stream().map(Message::getOriginalTargetId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toUnmodifiableMap(Article::getId, a->a));
        return result.map(m->{
            var vo = new CommentMessageVo(m);
            var comment = commentMap.get(m.getTargetId());
            vo.setAdditionInfo(articleMap.get(m.getOriginalTargetId()).getTitle());
            vo.setIsAgreed(isAgreedCommentIds.contains(m.getTargetId()));//是否已点赞
            vo.setIsAnonymous(comment.getIsAnonymous());
            vo.setRemoved(comment.getRemoved());
            return vo;
        });
    }

    @Override
    public Page<CommentMessageVo> getLeftMessagePage(BaseUser user, Pageable pageable) {
        var result = messageRepository
                .findAll(
                        MessageSpecification.toUser(user).and(MessageSpecification.type(Message.Type.LEFT_MESSAGE)),
                        pageable
                );
        var commentIds = result.stream().map(Message::getTargetId).collect(Collectors.toSet());
//        查询评论
        var commentMap = commentRepository.findAllById(commentIds).stream().collect(Collectors.toUnmodifiableMap(Comment::getId, c->c));
//        是否点赞
        var isAgreedCommentIds = commentAgreeRepository.findAllByCreatedUserAndCommentIdIn(user, commentIds)
                .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
        return result.map(m->{
            var vo = new CommentMessageVo(m);
            var comment = commentMap.get(m.getTargetId());
            vo.setIsAgreed(isAgreedCommentIds.contains(m.getTargetId()));//是否已点赞
            vo.setIsAnonymous(comment.getIsAnonymous());
            vo.setRemoved(comment.getRemoved());
            return vo;
        });
    }

    @Override
    public Page<CommentMessageVo> getReplyMessagePage(BaseUser baseUser, Pageable pageable) {
        var result = messageRepository.findAll(
                MessageSpecification.toUser(baseUser).and(
                        MessageSpecification.typeLike("REPLY")
                ),
                pageable
        );

//        起始目标评论(被回复的内容)，即评论 或者 评论的回复
        var commentsOriginalTargetMessages = new ArrayList<Message>();//目标是评论的消息
        var replyOriginalTargetMessages = new ArrayList<Message>();//目标是回复的消息
        for (Message message : result) {
            switch (message.getType()){
                case ARTICLE_COMMENT_REPLY -> commentsOriginalTargetMessages.add(message);
                case ARTICLE_SUB_COMMENT_REPLY -> replyOriginalTargetMessages.add(message);
            }
        }
        var originalTargetComments = commentsOriginalTargetMessages.isEmpty() ? Collections.<Long, Comment>emptyMap() : commentRepository.findAllById(
                        commentsOriginalTargetMessages
                                .stream()
                                .map(Message::getOriginalTargetId).collect(Collectors.toSet())
                )
                .stream().collect(Collectors.toUnmodifiableMap(Comment::getId, c->c));
        var originalTargetReplies = replyOriginalTargetMessages.isEmpty() ? Collections.<Long, CommentReply>emptyMap() : commentReplyRepository.findAllById(
                        replyOriginalTargetMessages
                                .stream()
                                .map(Message::getOriginalTargetId).collect(Collectors.toSet())
                )
                .stream().collect(Collectors.toUnmodifiableMap(CommentReply::getId, c->c));
//        获取当前回复
        var targetComments = commentsOriginalTargetMessages.isEmpty() ? Collections.<Long, Comment>emptyMap() : commentRepository.findAllById(
                        commentsOriginalTargetMessages
                                .stream()
                                .map(Message::getTargetId).collect(Collectors.toSet())
                )
                .stream().collect(Collectors.toUnmodifiableMap(Comment::getId, c->c));
        var targetReplies = replyOriginalTargetMessages.isEmpty() ? Collections.<Long, CommentReply>emptyMap() : commentReplyRepository.findAllById(
                        replyOriginalTargetMessages
                                .stream()
                                .map(Message::getTargetId).collect(Collectors.toSet())
                )
                .stream().collect(Collectors.toUnmodifiableMap(CommentReply::getId, c->c));

//        查询是否点赞
        var isAgreedCommentIds = commentReplyAgreeRepository.findAllByCreatedUserAndCommentIdIn(
                baseUser,
                result.getContent().stream().map(Message::getTargetId).collect(Collectors.toSet())
        ).stream().map(a->a.getComment().getId()).collect(Collectors.toSet());

//        匹配@到的人
        var msgIdAndMemberNameListMap = new HashMap<Long, Set<String>>();
        for (Message message : result) {
            var matcher = RegexpConstant.COMMENT_MEMBER.matcher(message.getContent());

            while (matcher.find()) msgIdAndMemberNameListMap.computeIfAbsent(message.getId(), k->new HashSet<>()).add(matcher.group(1));
        }
        var memberMap = baseUserRepository.findAllByUsernameIn(
                msgIdAndMemberNameListMap.values().stream()
                .reduce(new HashSet<>(), (c,i)->{
                    c.addAll(i);
                    return c;
                })
        )
                .stream()
                .collect(Collectors.toUnmodifiableMap(BaseUser::getUsername, u->u));

        return result.map(m->{
            var vo = new CommentMessageVo(m);
            //附加信息显示自己发送给的消息，也就是当前消息的回复对象
            switch (m.getType()){
                case ARTICLE_COMMENT_REPLY -> {
                    vo.setAdditionInfo(originalTargetComments.get(m.getOriginalTargetId()).getContent());
                    var targetComment = targetComments.get(m.getTargetId());
                    vo.setIsAnonymous(targetComment.getIsAnonymous());
                    vo.setRemoved(targetComment.getRemoved());
                }
                case ARTICLE_SUB_COMMENT_REPLY -> {
                    vo.setAdditionInfo(originalTargetReplies.get(m.getOriginalTargetId()).getContent());
                    var targetReply = targetReplies.get(m.getTargetId());
                    vo.setIsAnonymous(targetReply.getIsAnonymous());
                    vo.setRemoved(targetReply.getIsAnonymous());
                }
            }

            vo.setIsAgreed(isAgreedCommentIds.contains(m.getRelatedTargetId()));
            var memberNameSet = msgIdAndMemberNameListMap.get(m.getId());
            if(memberNameSet != null){
                vo.setMembers(memberNameSet.stream().map(memberMap::get).filter(Objects::nonNull).map(BaseUserVo::new).collect(Collectors.toList()));
            }
            return vo;
        });
    }

    @Override
    public Page<CommentMessageVo> getMentionMessagePage(BaseUser baseUser, Pageable pageable) {
        var messagePage = messageRepository.findAll(
                MessageSpecification.toUser(baseUser)
                        .and(MessageSpecification.typeLike("MENTION")),
                pageable
        );

        var commentMentionMessages = new ArrayList<Message>();//一级评论的消息
        var commentReplyMentionMessages = new ArrayList<Message>();//二级评论的消息
        var msgIdAndMemberListMap = new HashMap<Long, Set<String>>();//消息对应@到的用户列表
        for (Message message : messagePage) {
            switch (message.getType()) {
                case ARTICLE_COMMENT_MENTION, LEFT_MESSAGE_MENTION -> commentMentionMessages.add(message);
                case ARTICLE_SUB_COMMENT_MENTION, LEFT_MESSAGE_REPLY_MENTION -> commentReplyMentionMessages.add(message);
            }
        }

//        查询评论内容
        var commentMap = commentRepository.findAllById(commentMentionMessages.stream().map(Message::getTargetId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toUnmodifiableMap(Comment::getId, c->c));
        var replyMap = commentReplyRepository.findAllById(commentReplyMentionMessages.stream().map(Message::getTargetId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toUnmodifiableMap(CommentReply::getId, c->c));

//            匹配@到的用户
        for (Message message : messagePage) {
            String content = "";
            switch (message.getType()){
                case ARTICLE_COMMENT_MENTION -> {
                    var comment = commentMap.get(message.getTargetId());
                    if(!comment.getRemoved()) content = comment.getContent();
                }
                case ARTICLE_SUB_COMMENT_MENTION -> {
                    var reply = replyMap.get(message.getTargetId());
                    if(!reply.getRemoved()) content = reply.getContent();
                }
            }
            message.setContent(content);
            if(content.isEmpty()) continue;
            var matcher = RegexpConstant.COMMENT_MEMBER.matcher(content);
            var memberList = msgIdAndMemberListMap.computeIfAbsent(message.getId(), k->new HashSet<>());
            while (matcher.find()) memberList.add(matcher.group(1));
        }

//        查询是否点赞
        var commentAgreedIds = commentAgreeRepository.findAllByCreatedUserAndCommentIdIn(
                baseUser,
                commentMentionMessages.stream().map(Message::getTargetId).collect(Collectors.toList())
        )
                .stream().map(a->a.getComment().getId())
                .collect(Collectors.toSet());
        var commentReplyAgreedIds = commentReplyAgreeRepository.findAllByCreatedUserAndCommentIdIn(
                baseUser,
                commentReplyMentionMessages.stream().map(Message::getTargetId).collect(Collectors.toList())
        )
                .stream().map(a->a.getComment().getId())
                .collect(Collectors.toSet());

//        查询@的用户
        var memberMap = baseUserRepository.findAllByUsernameIn(
                msgIdAndMemberListMap.values().stream().reduce(new HashSet<>(), (c, i)->{
                    c.addAll(i);
                    return c;
                })
        ).stream().collect(Collectors.toUnmodifiableMap(BaseUser::getUsername, u->u));

        return messagePage.map(msg->{
            var vo = new CommentMessageVo(msg);
            switch (msg.getType()){
                case ARTICLE_COMMENT_MENTION, LEFT_MESSAGE_MENTION -> {
                    var comment = commentMap.get(msg.getTargetId());
                    vo.setIsAgreed(commentAgreedIds.contains(msg.getTargetId()));
                    vo.setRemoved(comment.getRemoved());
                    vo.setIsAnonymous(comment.getIsAnonymous());
                }
                case ARTICLE_SUB_COMMENT_MENTION, LEFT_MESSAGE_REPLY_MENTION -> {
                    var comment = replyMap.get(msg.getTargetId());
                    vo.setIsAgreed(commentReplyAgreedIds.contains(msg.getTargetId()));
                    vo.setRemoved(comment.getRemoved());
                    vo.setIsAnonymous(comment.getIsAnonymous());
                }
            }
            var memberNames = msgIdAndMemberListMap.get(msg.getId());
            if(memberNames != null){
                vo.setMembers(
                        memberNames.stream()
                                .map(memberMap::get)
                                .filter(Objects::nonNull)
                                .map(BaseUserVo::new)
                                .collect(Collectors.toList())
                );
            }
            return vo;
        });
    }

    @Override
    public Page<MessageVo> getAgreeMessagePage(BaseUser baseUser, Pageable pageable) {
        var result = messageRepository.findAll(
                MessageSpecification.toUser(baseUser)
                        .and(MessageSpecification.typeLike("AGREE")),
                pageable
        );

//        分类
        var articleAgreeMessageList = new ArrayList<Message>();
        var commentAgreeMessageList = new ArrayList<Message>();
        var subCommentAgreeMessageList = new ArrayList<Message>();
        for (Message message : result) {
            if(Message.Type.ARTICLE_AGREE.equals(message.getType())){
                articleAgreeMessageList.add(message);
            }else if(Message.Type.ARTICLE_COMMENT_AGREE.equals(message.getType())){
                commentAgreeMessageList.add(message);
            }else{
                subCommentAgreeMessageList.add(message);
            }
        }

//        查询附加信息
        var articleMap = articleAgreeMessageList.isEmpty() ?
                Collections.<Long, Article>emptyMap() :
                articleRepository.findAllById(articleAgreeMessageList.stream().map(Message::getRelatedTargetId).collect(Collectors.toList()))
                .stream().collect(Collectors.toUnmodifiableMap(Article::getId, a->a));
        var commentMap = commentAgreeMessageList.isEmpty() ?
                Collections.<Long, Comment>emptyMap() :
                commentRepository.findAllById(commentAgreeMessageList.stream().map(Message::getRelatedTargetId).collect(Collectors.toList()))
                .stream().collect(Collectors.toUnmodifiableMap(Comment::getId, c->c));
        var subCommentMap = subCommentAgreeMessageList.isEmpty() ?
                Collections.<Long, CommentReply>emptyMap() :
                commentReplyRepository.findAllById(subCommentAgreeMessageList.stream().map(Message::getRelatedTargetId).collect(Collectors.toList()))
                .stream().collect(Collectors.toUnmodifiableMap(CommentReply::getId, c->c));

        return result.map(msg->{
            var vo = new MessageVo(msg);
            if(Message.Type.ARTICLE_AGREE.equals(msg.getType())){
                var article = articleMap.get(msg.getRelatedTargetId());
                if(article != null) vo.setAdditionInfo(article.getTitle());
            }else if(Message.Type.ARTICLE_COMMENT_AGREE.equals(msg.getType())){
                var comment = commentMap.get(msg.getRelatedTargetId());
                if(comment != null) vo.setAdditionInfo(comment.getContent());
            }else{
                var subComment = subCommentMap.get(msg.getRelatedTargetId());
                if(subComment != null) vo.setAdditionInfo(subComment.getContent());
            }
            return vo;
        });
    }

    @Override
    public Page<MessageVo> getSystemMessagePage(BaseUser baseUser, Pageable pageable) {
        return messageRepository.findAll(
                MessageSpecification.toUser(baseUser).and(
                        MessageSpecification.type(Message.Type.SYSTEM)
                ),
                pageable
        )
                .map(MessageVo::new);
    }

    @Override
    public List<PrivateMessageSessionVo> getPrivateMessageSessionList(BaseUser baseUser) {
        return messageRepository.findAllPrivateMessageSessionByToUserId(baseUser.getId())
                .stream().map(PrivateMessageSessionVo::new).collect(Collectors.toList());
    }

    @Override
    public Page<MessageVo> getPrivateMessagePage(Long myId, Long hisId, Pageable pageable) {
        return messageRepository.findAllByToUserIdAndFromUserId(myId, hisId, pageable).map(MessageVo::new);
    }

    @Override
    public MessageVo sendMessage(BaseUser fromUser, MessageDto dto) {
        var toUser = baseUserRepository.findById(dto.getToUserId()).orElseThrow(NotFoundException::new);
        var msg = new Message();
        msg.setIsRead(false);
        msg.setFromUser(fromUser);
        msg.setToUser(toUser);
        msg.setContent(dto.getContent());
        return new MessageVo(messageRepository.save(msg));
    }

    @Transactional
    @Override
    public ValueVo<Boolean> setRead(BaseUser user, Long... ids) {
        var msgList = messageRepository.findAllById(Arrays.asList(ids));
//        必须全部是自己收到的消息
        if(msgList.stream().anyMatch(m->!m.getToUser().getId().equals(user.getId()))) throw new ForbiddenException();
        messageRepository.setRead(ids);
        return new ValueVo<>(true);
    }
}
