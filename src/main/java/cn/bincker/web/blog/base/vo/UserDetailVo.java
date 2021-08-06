package cn.bincker.web.blog.base.vo;

import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDetailVo extends BaseUserVo{
    private Long agreedNum;
    private Long articleNum;
    private Long commentNum;

    public UserDetailVo(BaseUser user) {
        super(user);
    }
}
