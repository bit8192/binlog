package cn.bincker.web.blog.base.exception;

/**
 * 系统错误，500异常
 */
public class SystemException extends RuntimeException{
    public SystemException() {
        this("系统繁忙");
    }

    public SystemException(String s) {
        super(s);
    }

    public SystemException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SystemException(Throwable throwable) {
        super(throwable);
    }

    public SystemException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
