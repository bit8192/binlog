package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleCommentListVo {
    private Long id;

    private String content;

    private BaseUserVo createdUser;

    private Date createdDate;

    private Long agreeNum;

    private Long treadNum;

    private List<ArticleCommentVo> replies;

    private Long repliesNum;

    private Boolean isAgreed;

    private Boolean isTrod;
}
