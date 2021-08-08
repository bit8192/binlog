package cn.bincker.web.blog.material.service;

import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.vo.CommentVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.material.dto.ArticleDto;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.vo.ArticleListVo;
import cn.bincker.web.blog.material.vo.ArticleVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IArticleService {
    ArticleVo getDetail(Long articleId);

    Page<ArticleListVo> pageAll(String keywords, Long articleClassId, Long[] tagIds, Pageable pageable);

    ArticleVo update(ArticleDto articleDto);

    ArticleVo insert(ArticleDto articleDto);

    void delete(Long articleId);

    /**
     * 切换点赞
     * 返回当前是否已点赞
     */
    ValueVo<Boolean> toggleAgreed(Long articleId);

    /**
     * 提交阅读，增加阅读量
     */
    void view(Long articleId);

    /**
     * 查询评论
     */
    Page<CommentVo> getCommentPage(Long articleId, Pageable pageable);

    /**
     * 评论
     */
    CommentVo commenting(Long articleId, CommentDto dto);
}
