package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.repository.IMessageRepository;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.material.dto.ArticleSubCommentDto;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import cn.bincker.web.blog.material.entity.ArticleSubCommentAgree;
import cn.bincker.web.blog.material.entity.ArticleSubCommentTread;
import cn.bincker.web.blog.material.repository.IArticleCommentRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentAgreeRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentTreadRepository;
import cn.bincker.web.blog.material.service.IArticleSubCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import cn.bincker.web.blog.material.vo.IArticleMemberCommentVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleSubCommentServiceImpl implements IArticleSubCommentService {
    private static final Logger log = LoggerFactory.getLogger(ArticleSubCommentServiceImpl.class);

    private final IArticleSubCommentRepository articleSubCommentRepository;
    private final IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository;
    private final IArticleSubCommentTreadRepository articleSubCommentTreadRepository;
    private final UserAuditingListener userAuditingListener;
    private final IArticleCommentRepository articleCommentRepository;
    private final IBaseUserRepository baseUserRepository;
    private final IMessageRepository messageRepository;

    public ArticleSubCommentServiceImpl(IArticleSubCommentRepository articleSubCommentRepository, IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository, IArticleSubCommentTreadRepository articleSubCommentTreadRepository, UserAuditingListener userAuditingListener, IArticleCommentRepository articleCommentRepository, IBaseUserRepository baseUserRepository, IMessageRepository messageRepository) {
        this.articleSubCommentRepository = articleSubCommentRepository;
        this.articleSubCommentAgreeRepository = articleSubCommentAgreeRepository;
        this.articleSubCommentTreadRepository = articleSubCommentTreadRepository;
        this.userAuditingListener = userAuditingListener;
        this.articleCommentRepository = articleCommentRepository;
        this.baseUserRepository = baseUserRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ArticleCommentVo comment(ArticleSubCommentDto dto) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var articleComment = articleCommentRepository.findById(dto.getCommentId()).orElseThrow(NotFoundException::new);
        var subCommentTotal = articleSubCommentRepository.countByTarget(articleComment);
        var comment = new ArticleSubComment();
        comment.setContent(dto.getContent());
        comment.setTarget(articleComment);
        comment.setCreatedUser(user);
        comment.setRecommend(subCommentTotal < 3);
        articleSubCommentRepository.save(comment);

//        发送消息提醒作者
        if(!user.getId().equals(articleComment.getCreatedUser().getId()))
            sendMessage(Message.Type.ARTICLE_COMMENT_REPLY, comment.getContent(), comment.getId(), user, articleComment.getCreatedUser());
//        发送消息提醒被@的人
        List<BaseUser> memberList = sendMentionUserMessage(user, comment.getContent(), comment.getId());

        var vo = new ArticleCommentVo(comment);
        vo.setMembers(memberList.stream().map(BaseUserVo::new).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 发送消息提醒被@到的用户
     * @param user 当前评论的用户
     */
    @SuppressWarnings("DuplicatedCode")
    private List<BaseUser> sendMentionUserMessage(BaseUser user, String content, Long relevantId) {
        var matcher = RegexpConstant.COMMENT_MEMBER.matcher(content);
        var memberNameList = new ArrayList<String>();
        while (matcher.find()) memberNameList.add(matcher.group(2));
        var memberList = baseUserRepository.findAllByUsernameIn(memberNameList);
        var mentionMessageList = memberList.stream().filter(u->!u.getId().equals(user.getId())).map(u->{
            var msg = new Message();
            msg.setType(Message.Type.ARTICLE_SUB_COMMENT_MENTION);
            msg.setFromUser(user);
            msg.setToUser(u);
            msg.setContent(content);
            msg.setRelevantId(relevantId);
            return msg;
        }).collect(Collectors.toList());
        if(!mentionMessageList.isEmpty()) messageRepository.saveAll(mentionMessageList);
        return memberList;
    }

    /**
     * 发送消息通知
     */
    @SuppressWarnings("DuplicatedCode")
    private void sendMessage(Message.Type type, String content, Long relevantId, BaseUser fromUser, BaseUser toUser) {
        var msg = new Message();
        msg.setFromUser(fromUser);
        msg.setToUser(toUser);
        msg.setIsRead(false);
        msg.setContent(content);
        msg.setType(type);
        msg.setRelevantId(relevantId);
        messageRepository.save(msg);
    }

    @Override
    public ArticleCommentVo subComment(ArticleSubCommentDto dto) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var articleSubComment = articleSubCommentRepository.findById(dto.getCommentId()).orElseThrow(NotFoundException::new);
        var targetUser = baseUserRepository.getOne(articleSubComment.getCreatedUser().getId());
        var subCommentTotal = articleSubCommentRepository.countByTarget(articleSubComment.getTarget());
        var comment = new ArticleSubComment();
        comment.setContent("回复 @" + targetUser.getUsername() + " :" + dto.getContent());
        comment.setTarget(articleSubComment.getTarget());
        comment.setCreatedUser(user);
        comment.setRecommend(subCommentTotal < 3);
        articleSubCommentRepository.save(comment);

//        发送消息提醒被评论用户
        if(!user.getId().equals(articleSubComment.getCreatedUser().getId()))
            sendMessage(Message.Type.ARTICLE_SUB_COMMENT_REPLY, comment.getContent(), comment.getId(), user, articleSubComment.getCreatedUser());
//        发送消息提醒被@到的用户
        var memberList = sendMentionUserMessage(user, comment.getContent(), comment.getId());

        var vo = new ArticleCommentVo(comment);
        vo.setMembers(memberList.stream().map(BaseUserVo::new).collect(Collectors.toList()));
        return vo;
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

        var agreeOptional = articleSubCommentAgreeRepository.findByCreatedUserAndComment(user, comment);
        var treadOptional = articleSubCommentTreadRepository.findByCreatedUserAndComment(user, comment);
        if(agreeOptional.isEmpty()){
            var agree = new ArticleSubCommentAgree();
            agree.setComment(comment);
            articleSubCommentAgreeRepository.save(agree);
            treadOptional.ifPresent(articleSubCommentTreadRepository::delete);
            if(!user.getId().equals(comment.getCreatedUser().getId()))
                sendMessage(Message.Type.ARTICLE_SUB_COMMENT_AGREE, null, id, user, comment.getCreatedUser());
        }else{
            articleSubCommentAgreeRepository.delete(agreeOptional.get());
        }

        updateArticleSubCommentAgreeAndTreadNum(id, comment);

        return new ValueVo<>(agreeOptional.isEmpty());
    }

    /**
     * 更新子评论赞和踩的数量
     */
    private void updateArticleSubCommentAgreeAndTreadNum(Long id, ArticleSubComment comment) {
        synchronized (SynchronizedPrefixConstant.TOGGLE_ARTICLE_SUB_COMMENT_AGREE_AND_TREAD + id) {
            //重新统计数量
            var agreeNum = articleSubCommentAgreeRepository.countByComment(comment);
            var treadNum = articleSubCommentTreadRepository.countByComment(comment);
            comment.setAgreedNum(agreeNum);
            comment.setTreadNum(treadNum);
            articleSubCommentRepository.save(comment);
        }
    }

    @Override
    @Transactional
    public ValueVo<Boolean> toggleTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleSubCommentRepository.findById(id).orElseThrow(NotFoundException::new);

        var treadOptional = articleSubCommentTreadRepository.findByCreatedUserAndComment(user, comment);
        var agreeOptional = articleSubCommentAgreeRepository.findByCreatedUserAndComment(user, comment);
        if(treadOptional.isEmpty()){
            var tread = new ArticleSubCommentTread();
            tread.setComment(comment);
            articleSubCommentTreadRepository.save(tread);
            agreeOptional.ifPresent(articleSubCommentAgreeRepository::delete);
        }else{
            articleSubCommentTreadRepository.delete(treadOptional.get());
        }

        //重新统计数量
        updateArticleSubCommentAgreeAndTreadNum(id, comment);
        return new ValueVo<>(treadOptional.isEmpty());
    }

    @Override
    public Page<ArticleCommentVo> getPage(Long articleCommentId, Pageable pageable) {
        var articleComment = articleCommentRepository.findById(articleCommentId).orElseThrow(NotFoundException::new);
        var page = articleSubCommentRepository.findAllByTarget(articleComment, pageable);
        var userOptional = userAuditingListener.getCurrentAuditor();
        var voPage = page.map(ArticleCommentVo::new);
        //查询点赞和点踩
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
        }else{
            for (ArticleCommentVo comment : voPage) {
                comment.setIsAgreed(false);
                comment.setIsTrod(false);
            }
        }
        handleMember(voPage.getContent());
        return voPage;
    }

    /**
     * 查询@的用户, 并设置到Vo中
     */
    @SuppressWarnings("DuplicatedCode")//service最好不互相调用，所以还是拷贝一份
    private void handleMember(List<? extends IArticleMemberCommentVo> commentVos){
        var commentMembersMap = new HashMap<String, Set<IArticleMemberCommentVo>>();
        for (var comment : commentVos) {
            var matcher = RegexpConstant.COMMENT_MEMBER.matcher(comment.getContent());
            while (matcher.find()){
                var username = matcher.group(2);
                var list = commentMembersMap.computeIfAbsent(username, k -> new HashSet<>());
                list.add(comment);
            }
        }
        var members = baseUserRepository.findAllByUsernameIn(commentMembersMap.keySet());
        for (BaseUser member : members) {
            var userVo = new BaseUserVo(member);
            for (var vo : commentMembersMap.get(member.getUsername())) {
                vo.getMembers().add(userVo);
            }
        }
    }
}
