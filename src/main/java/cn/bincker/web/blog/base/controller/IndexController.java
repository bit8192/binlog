package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.config.GithubAuthorizeConfigProperties;
import cn.bincker.web.blog.base.config.QQAuthorizeConfigProperties;
import cn.bincker.web.blog.base.config.SystemProfile;
import cn.bincker.web.blog.base.service.IIpAddressQueryService;
import cn.bincker.web.blog.base.vo.SystemProfileVo;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("${binlog.base-path}")
public class IndexController {
    private final IVerifyCode<?> verifyCode;
    private final SystemProfile profile;
    private final QQAuthorizeConfigProperties qqAuthorizeConfigProperties;
    private final IIpAddressQueryService ipAddressQueryService;
    private final GithubAuthorizeConfigProperties githubAuthorizeConfigProperties;

    public IndexController(IVerifyCode<?> verifyCode, SystemProfile profile, QQAuthorizeConfigProperties qqAuthorizeConfigProperties, IIpAddressQueryService ipAddressQueryService, GithubAuthorizeConfigProperties githubAuthorizeConfigProperties) {
        this.verifyCode = verifyCode;
        this.profile = profile;
        this.qqAuthorizeConfigProperties = qqAuthorizeConfigProperties;
        this.ipAddressQueryService = ipAddressQueryService;
        this.githubAuthorizeConfigProperties = githubAuthorizeConfigProperties;
    }

    @GetMapping(value = "verify-code", produces = MediaType.IMAGE_JPEG_VALUE)
    public void verifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        verifyCode.write(request, response);
    }

    @GetMapping("profile")
    public SystemProfileVo profile(HttpServletRequest request){
        var vo = new SystemProfileVo(profile);
        vo.setUseQQAuthorize(qqAuthorizeConfigProperties.isUse());
        vo.setUseGithubAuthorize(githubAuthorizeConfigProperties.isUse());
        var ip = RequestUtils.getRequestIp(request);
        var address = ipAddressQueryService.query(ip);
        var userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        return vo;
    }
}
