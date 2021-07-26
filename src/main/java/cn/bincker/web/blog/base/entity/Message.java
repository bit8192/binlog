package cn.bincker.web.blog.base.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Message extends BaseEntity{
    private String content;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne
    private BaseUser fromUser;

    @ManyToOne
    private BaseUser toUser;

    /**
     * 相关对象ID
     * 评论文章消息存储该评论的id
     * 评论回复存储回复的id
     * 子评论回复存储回复的id
     */
    private Long relevantId;

    /**
     * 是否已读
     */
    private Boolean isRead;

    public enum Type{
        //系统消息
        SYSTEM,
        //评论文章
        ARTICLE_COMMENT,
        //文章评论回复
        ARTICLE_COMMENT_REPLY,
        //文章子评论回复
        ARTICLE_SUB_COMMENT_REPLY,
        //文章评论@到
        ARTICLE_COMMENT_MENTION,
        //文章子评论@到
        ARTICLE_SUB_COMMENT_MENTION,
        //文章获赞
        ARTICLE_AGREE,
        //文章评论获赞
        ARTICLE_COMMENT_AGREE,
        //文章子评论获赞
        ARTICLE_SUB_COMMENT_AGREE,
        //私信
        PRIVATE_MESSAGE
    }
}
