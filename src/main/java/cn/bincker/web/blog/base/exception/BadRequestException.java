package cn.bincker.web.blog.base.exception;

/**
 * 无效请求异常
 */
public class BadRequestException extends RuntimeException{
    public BadRequestException() {
    }

    public BadRequestException(String s) {
        super(s);
    }

    public BadRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BadRequestException(Throwable throwable) {
        super(throwable);
    }

    public BadRequestException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
