package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.service.IBaseUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BaseUserServiceImpl implements IBaseUserService {
    private final IBaseUserRepository repository;

    public BaseUserServiceImpl(IBaseUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<BaseUser> userOptional = repository.findByUsername(s);
        return new AuthorizationUser(userOptional.orElseThrow(()->new UsernameNotFoundException("用户不存在")));
    }

    @Override
    public BaseUser getByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }
}
