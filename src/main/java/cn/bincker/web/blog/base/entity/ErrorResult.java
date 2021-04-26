package cn.bincker.web.blog.base.entity;

import lombok.Data;

/**
 * 反馈给客户端的异常信息
 */
@Data
public class ErrorResult {
    private String msg;

    public ErrorResult(String msg) {
        this.msg = msg;
    }
}
