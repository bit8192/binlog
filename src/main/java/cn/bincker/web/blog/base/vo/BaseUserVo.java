package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;

@Data
public class BaseUserVo {
    private Long id;
    private String username;
    private String headImg;

    public BaseUserVo() {
    }

    public BaseUserVo(BaseUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.headImg = user.getHeadImg();
    }
}
