package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.material.entity.ArticleComment;
import cn.bincker.web.blog.material.entity.ArticleSubComment;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ArticleCommentVo implements IArticleMemberCommentVo{
    private Long id;
    private String content;
    private BaseUserVo createdUser;
    private Date createdDate;
    private Long agreedNum;
    private Long treadNum;
    private Boolean isAgreed;
    private Boolean isTrod;
    private List<BaseUserVo> members = new ArrayList<>();

    public ArticleCommentVo(ArticleComment articleComment) {
        this.id = articleComment.getId();
        this.content = articleComment.getContent();
        this.createdUser = new BaseUserVo(articleComment.getCreatedUser());
        this.createdDate = articleComment.getCreatedDate();
        this.agreedNum = articleComment.getAgreedNum();
        this.treadNum = articleComment.getTreadNum();
    }

    public ArticleCommentVo(ArticleSubComment articleComment) {
        this.id = articleComment.getId();
        this.content = articleComment.getContent();
        this.createdUser = new BaseUserVo(articleComment.getCreatedUser());
        this.createdDate = articleComment.getCreatedDate();
        this.agreedNum = articleComment.getAgreedNum();
        this.treadNum = articleComment.getTreadNum();
    }
}
