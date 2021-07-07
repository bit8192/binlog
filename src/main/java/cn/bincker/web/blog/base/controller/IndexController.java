package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.config.QQAuthorizeConfigProperties;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.config.SystemProfile;
import cn.bincker.web.blog.base.vo.SuccessMsgVo;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.vo.SystemProfileVo;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("${system.base-path}")
public class IndexController {
    private final IVerifyCode<?> verifyCode;
    private final SystemProfile profile;
    private final QQAuthorizeConfigProperties qqAuthorizeConfigProperties;

    public IndexController(IVerifyCode<?> verifyCode, SystemProfile profile, QQAuthorizeConfigProperties qqAuthorizeConfigProperties) {
        this.verifyCode = verifyCode;
        this.profile = profile;
        this.qqAuthorizeConfigProperties = qqAuthorizeConfigProperties;
    }

    @GetMapping(value = "verify-code", produces = MediaType.IMAGE_JPEG_VALUE)
    public void verifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        verifyCode.write(request, response);
    }

    @GetMapping("profile")
    public SystemProfileVo profile(){
        return new SystemProfileVo(profile, qqAuthorizeConfigProperties.isUse());
    }

    @GetMapping("self-info")
    public BaseUser selfInfo(BaseUser user){
        if(user == null) throw new UnauthorizedException();
        return user;
    }

    /**
     * 注销
     */
    @PostMapping("api/logout")
    public SuccessMsgVo logout(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext == null) throw new SystemException("SecurityContext 为空");
        securityContext.setAuthentication(null);
        return new SuccessMsgVo("注销成功");
    }
}
