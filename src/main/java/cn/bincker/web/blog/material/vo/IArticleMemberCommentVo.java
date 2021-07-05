package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用于查询member时放在同一个列表里同时处理
 * @see cn.bincker.web.blog.material.service.impl.ArticleCommentServiceImpl#handleMember(List)
 */
public interface IArticleMemberCommentVo {
    String getContent();
    void setContent(String content);
    List<BaseUserVo> getMembers();
    void setMembers(List<BaseUserVo> members);
}
