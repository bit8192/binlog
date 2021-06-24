package cn.bincker.web.blog.material.vo;

/**
 * 获取评论分页时，需要查询的子评论总数
 */
public interface RepliesTotalVo {
    Long getCommentId();
    Long getCount();
}
