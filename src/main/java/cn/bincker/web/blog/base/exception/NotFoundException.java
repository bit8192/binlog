package cn.bincker.web.blog.base.exception;

import lombok.Getter;

public class NotFoundException extends RuntimeException {
    @Getter
    private final String tip;

    public NotFoundException(String tip, String message) {
        super(message);
        this.tip = tip;
    }

    public NotFoundException(String message){
        super(message);
        this.tip = message;
    }

    public NotFoundException() {
        this.tip = "资源不存在";
    }
}
