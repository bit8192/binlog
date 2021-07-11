package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface IBaseUserService extends UserDetailsService {
    BaseUser getByUsername(String username);

    Optional<BaseUser> findByQQOpenId(String openId);

    Optional<BaseUser> findByUsername(String userName);

    void changePassword(BaseUser user, String password);

    UserDetailVo getUserDetail(BaseUser user);
}
