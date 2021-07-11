package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import lombok.Data;

@Data
public class BaseUserVo {
    private Long id;
    private String username;
    private String headImg;
    private Boolean isBlogger;

    public BaseUserVo() {
    }

    public BaseUserVo(BaseUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.headImg = user.getHeadImg();
        //noinspection EqualsBetweenInconvertibleTypes
        this.isBlogger = user.getRoles().stream().anyMatch(r->r.equals(Role.RoleEnum.BLOGGER));
    }
}
