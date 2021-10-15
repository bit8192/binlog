package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.config.GithubAuthorizeConfigProperties;
import cn.bincker.web.blog.base.config.QQAuthorizeConfigProperties;
import cn.bincker.web.blog.base.config.SystemProfile;
import cn.bincker.web.blog.base.vo.SystemProfileVo;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class IndexController {
    private final IVerifyCode<?> verifyCode;
    private final SystemProfile profile;
    private final QQAuthorizeConfigProperties qqAuthorizeConfigProperties;
    private final GithubAuthorizeConfigProperties githubAuthorizeConfigProperties;

    public IndexController(IVerifyCode<?> verifyCode, SystemProfile profile, QQAuthorizeConfigProperties qqAuthorizeConfigProperties, GithubAuthorizeConfigProperties githubAuthorizeConfigProperties) {
        this.verifyCode = verifyCode;
        this.profile = profile;
        this.qqAuthorizeConfigProperties = qqAuthorizeConfigProperties;
        this.githubAuthorizeConfigProperties = githubAuthorizeConfigProperties;
    }

    @GetMapping
    public String index(Model model){
        model.addAttribute("msg", "hello");
        return "index";
    }

    @GetMapping(value = "verify-code", produces = MediaType.IMAGE_JPEG_VALUE)
    public void verifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        verifyCode.write(request, response);
    }

    @GetMapping("profile")
    @ResponseBody
    public SystemProfileVo profile(){
        var vo = new SystemProfileVo(profile);
        vo.setUseQQAuthorize(qqAuthorizeConfigProperties.isUse());
        vo.setUseGithubAuthorize(githubAuthorizeConfigProperties.isUse());
        return vo;
    }
}
