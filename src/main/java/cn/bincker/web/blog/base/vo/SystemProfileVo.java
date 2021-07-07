package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.config.SystemProfile;
import lombok.Data;

@Data
public class SystemProfileVo {
    private String name;
    private String copyRight;
    private String ipc;
    private String github;
    private Boolean useQQAuthorize;

    public SystemProfileVo(SystemProfile systemProfile, boolean useQQAuthorize) {
        this.name = systemProfile.getName();
        this.copyRight = systemProfile.getCopyRight();
        this.ipc = systemProfile.getIpc();
        this.github = systemProfile.getGithub();
        this.useQQAuthorize = useQQAuthorize;
    }
}
