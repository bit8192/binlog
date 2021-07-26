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
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.entity.ArticleCommentAgree;
import cn.bincker.web.blog.material.entity.ArticleCommentTread;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import cn.bincker.web.blog.material.repository.*;
import cn.bincker.web.blog.material.service.IArticleCommentService;
import cn.bincker.web.blog.material.vo.ArticleCommentListVo;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import cn.bincker.web.blog.material.vo.IArticleMemberCommentVo;
import cn.bincker.web.blog.material.vo.RepliesTotalVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final IBaseUserRepository baseUserRepository;
    private final IMessageRepository messageRepository;

    public ArticleCommentServiceImpl(IArticleCommentRepository articleCommentRepository, UserAuditingListener userAuditingListener, IArticleRepository articleRepository, IArticleCommentAgreeRepository articleCommentAgreeRepository, IArticleCommentTreadRepository articleCommentTreadRepository, IArticleSubCommentRepository articleSubCommentRepository, IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository, IArticleSubCommentTreadRepository articleSubCommentTreadRepository, IBaseUserRepository baseUserRepository, IMessageRepository messageRepository) {
        this.articleCommentRepository = articleCommentRepository;
        this.userAuditingListener = userAuditingListener;
        this.articleRepository = articleRepository;
        this.articleCommentAgreeRepository = articleCommentAgreeRepository;
        this.articleCommentTreadRepository = articleCommentTreadRepository;
        this.articleSubCommentRepository = articleSubCommentRepository;
        this.articleSubCommentAgreeRepository = articleSubCommentAgreeRepository;
        this.articleSubCommentTreadRepository = articleSubCommentTreadRepository;
        this.baseUserRepository = baseUserRepository;
        this.messageRepository = messageRepository;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    @Transactional
    public ArticleCommentVo comment(ArticleCommentDto dto) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var article = articleRepository.findById(dto.getArticleId()).orElseThrow(NotFoundException::new);
        var articleAuthor = baseUserRepository.getOne(article.getCreatedUser().getId());
        var comment = new ArticleComment();
        comment.setContent(dto.getContent());
        comment.setTarget(article);
        articleCommentRepository.save(comment);

//        发送消息提醒作者，如果是自己评论的则不发送
        if(!user.getId().equals(articleAuthor.getId()))
            sendMessage(Message.Type.ARTICLE_COMMENT, comment.getContent(), comment.getId(), user, articleAuthor);
//        发送消息提醒被@的人
        var matcher = RegexpConstant.COMMENT_MEMBER.matcher(dto.getContent());
        var memberNameList = new ArrayList<String>();
        while (matcher.find()) memberNameList.add(matcher.group(2));
        var memberList = baseUserRepository.findAllByUsernameIn(memberNameList);
        var mentionMessageList = memberList.stream().filter(u->!u.getId().equals(articleAuthor.getId())).map(u->{
            var msg = new Message();
            msg.setType(Message.Type.ARTICLE_COMMENT_MENTION);
            msg.setFromUser(user);
            msg.setToUser(u);
            msg.setContent(dto.getContent());
            msg.setRelevantId(comment.getId());
            return msg;
        }).collect(Collectors.toList());
        if(!mentionMessageList.isEmpty()) messageRepository.saveAll(mentionMessageList);

        comment.setCreatedUser(user);//不设置的话只有id
        var vo = new ArticleCommentVo(comment);
        vo.setMembers(memberList.stream().map(BaseUserVo::new).collect(Collectors.toList()));
        return vo;
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

        var agreeOptional = articleCommentAgreeRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
        var treadOptional = articleCommentTreadRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
        if(agreeOptional.isEmpty()){//如果没有点赞，那么点赞，并取消踩
            treadOptional.ifPresent(articleCommentTreadRepository::delete);
            var agree = new ArticleCommentAgree();
            agree.setComment(comment);
            articleCommentAgreeRepository.save(agree);
            if(!user.getId().equals(comment.getCreatedUser().getId())) sendMessage(Message.Type.ARTICLE_COMMENT_AGREE, null, id, user, comment.getCreatedUser());
        }else{
            articleCommentAgreeRepository.delete(agreeOptional.get());
        }

        updateCommentAgreeAndTreadNum(id, comment);

        return new ValueVo<>(agreeOptional.isEmpty());
    }

    /**
     * 更新评论点赞和踩的数量
     */
    private void updateCommentAgreeAndTreadNum(Long id, ArticleComment comment) {
        synchronized (SynchronizedPrefixConstant.TOGGLE_ARTICLE_COMMENT_AGREE_AND_TREAD + id) {
            //更新点赞数和踩数量
            var agreeNum = articleCommentAgreeRepository.countByCommentId(id);
            var treadNum = articleCommentTreadRepository.countByCommentId(id);
            comment.setAgreedNum(agreeNum);
            comment.setTreadNum(treadNum);
            articleCommentRepository.save(comment);
        }
    }

    @Override
    public ValueVo<Boolean> toggleTread(Long id) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var comment = articleCommentRepository.findById(id).orElseThrow(NotFoundException::new);

        var treadOptional = articleCommentTreadRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
        var agreeOptional = articleCommentAgreeRepository.findByCreatedUserIdAndCommentId(user.getId(), id);
        if(treadOptional.isEmpty()){//如果没有点踩，那么点踩，并取消赞
            agreeOptional.ifPresent(articleCommentAgreeRepository::delete);
            var tread = new ArticleCommentTread();
            tread.setComment(comment);
            articleCommentTreadRepository.save(tread);
        }else{
            articleCommentTreadRepository.delete(treadOptional.get());
        }

        updateCommentAgreeAndTreadNum(id, comment);
        return new ValueVo<>(treadOptional.isEmpty());
    }

    @Override
    public Page<ArticleCommentListVo> getPage(Long articleId, Pageable pageable) {
        var page = articleCommentRepository.findAllByTargetId(articleId, pageable);
        var contentIdList = page.getContent().stream().map(ArticleComment::getId).collect(Collectors.toList());
        //子评论
        var replies = articleSubCommentRepository.findAllByRecommendIsTrueAndTargetIn(page.getContent());
        var repliesIdList = replies.stream().map(ArticleSubComment::getId).collect(Collectors.toList());
        //子评论数量统计
        var repliesTotalMap = articleCommentRepository
                .getRepliesTotals(contentIdList)
                .stream()
                .collect(Collectors.toUnmodifiableMap(RepliesTotalVo::getCommentId, RepliesTotalVo::getCount));
        var voPage = page.map(comment->{
            var vo = new ArticleCommentListVo();
            vo.setId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setAgreedNum(comment.getAgreedNum());
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
            var articleSubCommentIsAgreedSet = articleSubCommentAgreeRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), repliesIdList)
                    .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            var articleSubCommentIsTrodSet = articleSubCommentTreadRepository.findByCreatedUserIdAndCommentIdIn(user.getId(), repliesIdList)
                    .stream().map(a->a.getComment().getId()).collect(Collectors.toSet());
            voPage.forEach(v->{
                v.setIsAgreed(articleCommentIsAgreedSet.contains(v.getId()));
                v.setIsTrod(articleCommentIsTrodSet.contains(v.getId()));
                v.getReplies().forEach(c->{
                    c.setIsAgreed(articleSubCommentIsAgreedSet.contains(c.getId()));
                    c.setIsTrod(articleSubCommentIsTrodSet.contains(c.getId()));
                });
            });
        }else {
            for (ArticleCommentListVo comment : voPage) {
                comment.setIsAgreed(false);
                comment.setIsTrod(false);
            }
        }
        //合并一起处理
        var memberCommentList = new ArrayList<IArticleMemberCommentVo>(voPage.getContent().size() + replies.size());
        memberCommentList.addAll(voPage.getContent());
        memberCommentList.addAll(replies.stream().map(ArticleCommentVo::new).collect(Collectors.toList()));
        handleMember(memberCommentList);
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
