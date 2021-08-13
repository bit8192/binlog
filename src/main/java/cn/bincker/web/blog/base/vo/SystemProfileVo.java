package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.config.SystemProfile;
import lombok.Data;

@Data
public class SystemProfileVo {
    private String name;
    private String copyRight;
    private String icp;
    private String github;
    private Boolean useQQAuthorize;
    private String expression;//happy or serious

    public SystemProfileVo(SystemProfile systemProfile, boolean useQQAuthorize) {
        this.name = systemProfile.getName();
        this.copyRight = systemProfile.getCopyRight();
        this.icp = systemProfile.getIcp();
        this.github = systemProfile.getGithub();
        this.useQQAuthorize = useQQAuthorize;
    }
}
