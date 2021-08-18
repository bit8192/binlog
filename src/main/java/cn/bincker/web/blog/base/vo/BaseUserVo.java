package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import lombok.Data;

@Data
public class BaseUserVo {
    private Long id;
    private String username;
    private String headImg;
    private String biography;
    private String website;
    private String github;
    private String qqOpenId;
    private String wechatOpenId;
    private Boolean isBlogger;
    private Boolean isAdmin;

    public BaseUserVo() {
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    public BaseUserVo(BaseUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.headImg = user.getHeadImg();
        this.biography = user.getBiography();
        this.website = user.getWebsite();
        this.github = user.getGithub();
        this.isBlogger = user.getRoles().stream().anyMatch(r->r.equals(Role.RoleEnum.BLOGGER));
        this.isAdmin = user.getRoles().stream().anyMatch(r->r.equals(Role.RoleEnum.ADMIN));
    }
}
