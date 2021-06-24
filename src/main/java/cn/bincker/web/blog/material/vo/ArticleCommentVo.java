package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import lombok.Data;

import java.util.Date;

@Data
public class ArticleCommentVo {
    private Long id;
    private String content;
    private BaseUserVo createdUser;
    private Date createdDate;
    private Long agreeNum;
    private Long treadNum;
    private Boolean isAgreed;
    private Boolean isTrod;

    public ArticleCommentVo(ArticleComment articleComment) {
        this.id = articleComment.getId();
        this.content = articleComment.getContent();
        this.createdUser = new BaseUserVo(articleComment.getCreatedUser());
        this.createdDate = articleComment.getCreatedDate();
        this.agreeNum = articleComment.getAgreedNum();
        this.treadNum = articleComment.getTreadNum();
    }

    public ArticleCommentVo(ArticleSubComment articleComment) {
        this.id = articleComment.getId();
        this.content = articleComment.getContent();
        this.createdUser = new BaseUserVo(articleComment.getCreatedUser());
        this.createdDate = articleComment.getCreatedDate();
        this.agreeNum = articleComment.getAgreedNum();
        this.treadNum = articleComment.getTreadNum();
    }
}
