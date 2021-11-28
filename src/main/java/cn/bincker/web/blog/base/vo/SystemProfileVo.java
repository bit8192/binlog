package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.config.SystemProfile;
import lombok.Data;

@Data
public class SystemProfileVo {
    private String name;
    private Boolean isDev;
    private String copyRight;
    private String icp;
    private String github;
    private Boolean useQQAuthorize;
    private Boolean useGithubAuthorize;
    private String expression;//happy or serious
    private Boolean allowRegister;

    public SystemProfileVo(SystemProfile systemProfile) {
        this.name = systemProfile.getName();
        this.isDev = systemProfile.getIsDev();
        this.copyRight = systemProfile.getCopyRight();
        this.icp = systemProfile.getIcp();
        this.github = systemProfile.getGithub();
        this.expression = systemProfile.getExpression();
        this.allowRegister = systemProfile.getAllowRegister();
    }
}
