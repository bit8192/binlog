package cn.bincker.web.blog.base.exception;

import lombok.Getter;

/**
 * 无效请求异常
 */
public class BadRequestException extends RuntimeException{
    @Getter
    private String tip;

    public BadRequestException() {
    }

    public BadRequestException(String s) {
        super(s);
    }

    public BadRequestException(String s, String tip) {
        super(s);
        this.tip = tip;
    }

    public BadRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BadRequestException(Throwable throwable) {
        super(throwable);
    }

    public BadRequestException(String s, String tip, Throwable throwable) {
        super(s, throwable);
        this.tip = tip;
    }

}
