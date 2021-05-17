package cn.bincker.web.blog.base.vo;

import lombok.Data;

/**
 * 是否成功消息
 */
@Data
public class SuccessMsgVo {
    private Boolean success;
    private String msg;

    public SuccessMsgVo(String msg) {
        this.success = true;
        this.msg = msg;
    }

    public SuccessMsgVo(Boolean success) {
        this.success = success;
    }

    public SuccessMsgVo(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }
}
