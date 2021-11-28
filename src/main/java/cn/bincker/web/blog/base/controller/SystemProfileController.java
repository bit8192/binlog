package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.config.GithubAuthorizeConfigProperties;
import cn.bincker.web.blog.base.config.QQAuthorizeConfigProperties;
import cn.bincker.web.blog.base.config.SystemProfile;
import cn.bincker.web.blog.base.vo.SystemProfileVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiController
public class SystemProfileController {
    private final SystemProfile profile;
    private final QQAuthorizeConfigProperties qqAuthorizeConfigProperties;
    private final GithubAuthorizeConfigProperties githubAuthorizeConfigProperties;

    public SystemProfileController(SystemProfile profile, QQAuthorizeConfigProperties qqAuthorizeConfigProperties, GithubAuthorizeConfigProperties githubAuthorizeConfigProperties) {
        this.profile = profile;
        this.qqAuthorizeConfigProperties = qqAuthorizeConfigProperties;
        this.githubAuthorizeConfigProperties = githubAuthorizeConfigProperties;
    }

    @GetMapping("profile")
    public SystemProfileVo profile(){
        var vo = new SystemProfileVo(profile);
        vo.setUseQQAuthorize(qqAuthorizeConfigProperties.isUse());
        vo.setUseGithubAuthorize(githubAuthorizeConfigProperties.isUse());
        return vo;
    }
}
