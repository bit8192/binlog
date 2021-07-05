package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ArticleCommentListVo implements IArticleMemberCommentVo {
    private Long id;

    private String content;

    private BaseUserVo createdUser;

    private Date createdDate;

    private Long agreedNum;

    private Long treadNum;

    private List<ArticleCommentVo> replies;

    private Long repliesNum;

    private Boolean isAgreed;

    private Boolean isTrod;

    private List<BaseUserVo> members = new ArrayList<>();
}
