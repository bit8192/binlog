package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.ErrorResult;
import cn.bincker.web.blog.base.exception.*;
import cn.bincker.web.blog.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestControllerAdvice
public class SystemAdviceController {
    private static final Logger log = LoggerFactory.getLogger(SystemAdviceController.class);

    private final UserAuditingListener userAuditingListener;

    public SystemAdviceController(UserAuditingListener userAuditingListener) {
        this.userAuditingListener = userAuditingListener;
    }

    @ResponseStatus
    @ExceptionHandler(SystemException.class)
    public ErrorResult systemExceptionHandle(SystemException exception){
        String msg = "系统异常";
        log.error(msg, exception);
        return new ErrorResult(msg);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ErrorResult badRequestExceptionHandle(BadRequestException exception, HttpServletRequest request){
        String msg = "无效请求";
        printLog(exception, request, msg);
        return new ErrorResult(msg);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ErrorResult unauthorizedExceptionHandle(UnauthorizedException exception, HttpServletRequest request){
        String msg = "未获授权请求";
        printLog(exception, request, msg);
        return new ErrorResult(msg);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResult notFoundExceptionHandle(NotFoundException exception){
        return new ErrorResult(exception.getTip());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ErrorResult forbiddenExceptionHandle(){
        return new ErrorResult("您没有权限访问");
    }

    private void printLog(Exception exception, HttpServletRequest request, String msg){
        Optional<BaseUser> userOptional = userAuditingListener.getCurrentAuditor();
        Long userId = -1L;
        if(userOptional.isPresent()) userId = userOptional.get().getId();
        log.error(msg + "\turl=" + request.getMethod() + " " + request.getRequestURL() + " " + request.getQueryString() + "\tip=" + CommonUtils.getRequestIp(request) + "\tuid=" + userId, exception);
    }
}
