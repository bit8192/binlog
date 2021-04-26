package cn.bincker.web.blog.base.exception;

/**
 * 401状态异常
 */
public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException() {
    }

    public UnauthorizedException(String s) {
        super(s);
    }

    public UnauthorizedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnauthorizedException(Throwable throwable) {
        super(throwable);
    }

    public UnauthorizedException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
