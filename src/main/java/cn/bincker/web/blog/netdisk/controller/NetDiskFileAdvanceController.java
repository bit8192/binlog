package cn.bincker.web.blog.netdisk.controller;

import cn.bincker.web.blog.base.entity.ErrorResult;
import cn.bincker.web.blog.netdisk.exception.MakeDirectoryFailException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NetDiskFileAdvanceController {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MakeDirectoryFailException.class)
    public ErrorResult makeDirectoryFailExceptionHandle(){
        return new ErrorResult("创建目录失败！请联系管理员进行处理！");
    }
}
