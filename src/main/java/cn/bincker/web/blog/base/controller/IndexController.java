package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.SystemProfile;
import cn.bincker.web.blog.base.entity.vo.SuccessMsgVo;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
public class IndexController {
    private final IVerifyCode<?> verifyCode;
    private final SystemProfile profile;

    public IndexController(IVerifyCode<?> verifyCode, SystemProfile profile) {
        this.verifyCode = verifyCode;
        this.profile = profile;
    }

    @GetMapping(value = "api/verify-code", produces = MediaType.IMAGE_JPEG_VALUE)
    public void verifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        verifyCode.write(request, response);
    }

    @GetMapping("api/profile")
    public EntityModel<SystemProfile> profile(){
        return EntityModel.of(profile);
    }

    @GetMapping("api/self-info")
    public EntityModel<BaseUser> selfInfo(BaseUser user){
        if(user == null) throw new UnauthorizedException();
        return EntityModel.of(user);
    }

    /**
     * 注销
     */
    @PostMapping("api/logout")
    public EntityModel<SuccessMsgVo> logout(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext == null) throw new SystemException("SecurityContext 为空");
        securityContext.setAuthentication(null);
        return EntityModel.of(new SuccessMsgVo("注销成功"));
    }
}
