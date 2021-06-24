package cn.bincker.web.blog.material.service;

import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.vo.ArticleCommentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IArticleSubCommentService {
    /**
     * 评论
     */
    ArticleCommentVo comment(Long articleCommentId, ArticleCommentDto dto);

    /**
     * 删除评论
     */
    void del(Long id);

    /**
     * 切换点赞
     * 点赞后会取消踩
     */
    ValueVo<Boolean> toggleAgree(Long id);

    /**
     * 切换踩
     * 踩后会取消点赞
     */
    ValueVo<Boolean> toggleTread(Long id);

    /**
     * 分页查询
     */
    Page<ArticleCommentVo> getPage(Long articleCommentId, Pageable pageable);
}
