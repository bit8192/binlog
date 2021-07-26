package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.dto.MessageDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.repository.IMessageRepository;
import cn.bincker.web.blog.base.service.IMessageService;
import cn.bincker.web.blog.base.specification.MessageSpecification;
import cn.bincker.web.blog.base.vo.*;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
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
    private final IArticleCommentRepository articleCommentRepository;
    private final IArticleSubCommentRepository articleSubCommentRepository;
    private final IArticleCommentAgreeRepository articleCommentAgreeRepository;
    private final IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository;

    public MessageServiceImpl(IMessageRepository messageRepository, IBaseUserRepository baseUserRepository, IArticleRepository articleRepository, IArticleCommentRepository articleCommentRepository, IArticleSubCommentRepository articleSubCommentRepository, IArticleCommentAgreeRepository articleCommentAgreeRepository, IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository) {
        this.messageRepository = messageRepository;
        this.baseUserRepository = baseUserRepository;
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
        this.articleSubCommentRepository = articleSubCommentRepository;
        this.articleCommentAgreeRepository = articleCommentAgreeRepository;
        this.articleSubCommentAgreeRepository = articleSubCommentAgreeRepository;
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
        var messageArticleCommentMap = result.getContent().stream().collect(Collectors.toUnmodifiableMap(Message::getId, Message::getRelevantId));
        var articleCommentIds = new HashSet<>(messageArticleCommentMap.values());
        var articleCommentMap = articleCommentRepository.findAllById(articleCommentIds).stream().collect(Collectors.toUnmodifiableMap(ArticleComment::getId, a->a));
        var isAgreedCommentIds = articleCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(baseUser.getId(), articleCommentIds)
                .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
        var articleMap = articleRepository.findAllById(articleCommentMap.values().stream().map(c->c.getTarget().getId()).collect(Collectors.toSet()))
                .stream().collect(Collectors.toUnmodifiableMap(Article::getId, a->a));
        return result.map(m->{
            var vo = new CommentMessageVo(m);
            vo.setAdditionInfo(
                    articleMap.get(
                            articleCommentMap.get(
                                    messageArticleCommentMap.get(m.getId())
                            ).getTarget().getId()
                    )
                            .getTitle()
            );
            vo.setIsAgreed(isAgreedCommentIds.contains(m.getRelevantId()));//是否已点赞
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

//        查询回复消息所回复的消息
        var commentMap = articleSubCommentRepository.findAllById(result.getContent().stream().map(Message::getRelevantId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toUnmodifiableMap(ArticleSubComment::getId, c->c));
        var commentTargetMap = articleSubCommentRepository.findAllById(commentMap.values().stream().map(c->c.getTarget().getId()).collect(Collectors.toSet()))
                .stream().collect(Collectors.toUnmodifiableMap(ArticleSubComment::getId, c->c));
//        查询是否点赞
        var isAgreedCommentIds = articleSubCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(
                baseUser.getId(),
                commentMap.keySet()
        ).stream().map(a->a.getComment().getId()).collect(Collectors.toSet());

//        匹配@到的人
        var msgIdAndMemberNameListMap = new HashMap<Long, Set<String>>();
        for (Message message : result) {
            var matcher = RegexpConstant.COMMENT_MEMBER.matcher(message.getContent());

            while (matcher.find()) msgIdAndMemberNameListMap.computeIfAbsent(message.getId(), k->new HashSet<>()).add(matcher.group(2));
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
            vo.setAdditionInfo(
                    commentTargetMap.get(
                            commentMap.get(m.getRelevantId()).getTarget().getId()
                    ).getContent()
            );
            vo.setIsAgreed(isAgreedCommentIds.contains(m.getRelevantId()));
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

        var articleCommentMessageMap = new HashMap<Long, Message>();//一级评论的消息
        var articleSubCommentMessageMap = new HashMap<Long, Message>();//二级评论的消息
        var msgIdAndMemberListMap = new HashMap<Long, Set<String>>();//消息对应@到的用户列表
        for (Message message : messagePage) {
            if(Message.Type.ARTICLE_COMMENT_MENTION.equals(message.getType())){
                articleCommentMessageMap.put(message.getId(), message);
            }else{
                articleSubCommentMessageMap.put(message.getId(), message);
            }
//            匹配@到的用户
            var matcher = RegexpConstant.COMMENT_MEMBER.matcher(message.getContent());
            var memberList = msgIdAndMemberListMap.computeIfAbsent(message.getId(), k->new HashSet<>());
            while (matcher.find()) memberList.add(matcher.group(2));
        }

//        查询是否点赞
        var articleCommentAgreedIds = articleCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(
                baseUser.getId(),
                articleCommentMessageMap.values().stream().map(Message::getRelevantId).collect(Collectors.toList())
        )
                .stream().map(a->a.getComment().getId())
                .collect(Collectors.toSet());
        var articleSubCommentAgreedIds = articleSubCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(
                baseUser.getId(),
                articleSubCommentMessageMap.values().stream().map(Message::getRelevantId).collect(Collectors.toList())
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
            if(articleCommentMessageMap.containsKey(msg.getId())){
                vo.setIsAgreed(articleCommentAgreedIds.contains(msg.getRelevantId()));
            }else{
                vo.setIsAgreed(articleSubCommentAgreedIds.contains(msg.getRelevantId()));
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
                articleRepository.findAllById(articleAgreeMessageList.stream().map(Message::getRelevantId).collect(Collectors.toList()))
                .stream().collect(Collectors.toUnmodifiableMap(Article::getId, a->a));
        var commentMap = commentAgreeMessageList.isEmpty() ?
                Collections.<Long, ArticleComment>emptyMap() :
                articleCommentRepository.findAllById(commentAgreeMessageList.stream().map(Message::getRelevantId).collect(Collectors.toList()))
                .stream().collect(Collectors.toUnmodifiableMap(ArticleComment::getId, c->c));
        var subCommentMap = subCommentAgreeMessageList.isEmpty() ?
                Collections.<Long, ArticleSubComment>emptyMap() :
                articleSubCommentRepository.findAllById(subCommentAgreeMessageList.stream().map(Message::getRelevantId).collect(Collectors.toList()))
                .stream().collect(Collectors.toUnmodifiableMap(ArticleSubComment::getId, c->c));

        return result.map(msg->{
            var vo = new MessageVo(msg);
            if(Message.Type.ARTICLE_AGREE.equals(msg.getType())){
                var article = articleMap.get(msg.getRelevantId());
                if(article != null) vo.setAdditionInfo(article.getTitle());
            }else if(Message.Type.ARTICLE_COMMENT_AGREE.equals(msg.getType())){
                var comment = commentMap.get(msg.getRelevantId());
                if(comment != null) vo.setAdditionInfo(comment.getContent());
            }else{
                var subComment = subCommentMap.get(msg.getRelevantId());
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
