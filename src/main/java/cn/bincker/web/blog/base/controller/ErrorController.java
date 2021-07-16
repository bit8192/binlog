package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.ErrorResult;
import cn.bincker.web.blog.base.exception.*;
import cn.bincker.web.blog.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping({"${server.error.path:${error.path:/error}}"})
@ControllerAdvice
public class ErrorController extends AbstractErrorController {
    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);

    private final UserAuditingListener userAuditingListener;
    private final String basePath;
    private final ErrorAttributeOptions allErrorAttributeOptions = ErrorAttributeOptions.of(
            ErrorAttributeOptions.Include.STACK_TRACE,
            ErrorAttributeOptions.Include.BINDING_ERRORS,
            ErrorAttributeOptions.Include.EXCEPTION,
            ErrorAttributeOptions.Include.MESSAGE
    );

    public ErrorController(ErrorAttributes errorAttributes, UserAuditingListener userAuditingListener, @Value("${system.base-path}") String basePath) {
        super(errorAttributes);
        this.userAuditingListener = userAuditingListener;
        this.basePath = basePath;
    }

    @Override
    public String getErrorPath() {
        return null;
    }

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void errorHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var errorAttributes = this.getErrorAttributes(request, allErrorAttributeOptions);
        var path = (String) errorAttributes.get("path");
        var status = (Integer) errorAttributes.get("status");
        if(status == HttpStatus.NOT_FOUND.value() && !path.startsWith(basePath)){
            response.sendRedirect("/?directPath=" + response.encodeURL(path));
        }
    }

    @RequestMapping
    public ResponseEntity<ErrorResult> error(HttpServletRequest request) {
        var errorAttributes = this.getErrorAttributes(request, allErrorAttributeOptions);
        var status = HttpStatus.valueOf((int) errorAttributes.get("status"));
        var message = (String) errorAttributes.get("message");
        if(message.equals("No message available")) message = "系统错误";
        return new ResponseEntity<>(new ErrorResult(message), status);
    }

    @ResponseStatus
    @ExceptionHandler(SystemException.class)
    @ResponseBody
    public ErrorResult systemExceptionHandle(SystemException exception){
        String msg = "系统异常";
        log.error(msg, exception);
        return new ErrorResult(msg);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ErrorResult badRequestExceptionHandle(BadRequestException exception, HttpServletRequest request){
        String msg;
        if(StringUtils.hasText(exception.getTip())){
            msg = exception.getTip();
        }else{
            msg = "无效请求";
        }
        printLog(exception, request, msg);
        return new ErrorResult(msg);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    public ErrorResult unauthorizedExceptionHandle(UnauthorizedException exception, HttpServletRequest request){
        String msg = "未获授权请求";
        printLog(exception, request, msg);
        return new ErrorResult(msg);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ErrorResult notFoundExceptionHandle(NotFoundException exception){
        return new ErrorResult(exception.getTip());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    @ResponseBody
    public ErrorResult forbiddenExceptionHandle(){
        return new ErrorResult("您没有权限访问");
    }

    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ExceptionHandler(NotImplementedException.class)
    @ResponseBody
    public ErrorResult notImplementedExceptionHandle(){
        return new ErrorResult("该功能未实现，请联系管理员");
    }

    private void printLog(Exception exception, HttpServletRequest request, String msg){
        Optional<BaseUser> userOptional = userAuditingListener.getCurrentAuditor();
        Long userId = -1L;
        if(userOptional.isPresent()) userId = userOptional.get().getId();
        log.error(msg + "\turl=" + request.getMethod() + " " + request.getRequestURL() + " " + request.getQueryString() + "\tip=" + RequestUtils.getRequestIp(request) + "\tuid=" + userId, exception);
    }
}