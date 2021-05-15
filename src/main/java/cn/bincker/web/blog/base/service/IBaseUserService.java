package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IBaseUserService extends UserDetailsService {
    BaseUser getByUsername(String username);
}
