package cn.bincker.web.blog.password.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.dto.PasswordGroupDto;
import cn.bincker.web.blog.password.entity.PasswordGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPasswordGroupService {
    Page<PasswordGroup> page(BaseUser user, String keywords, Pageable pageable);

    PasswordGroup add(PasswordGroupDto dto);

    PasswordGroup update(PasswordGroupDto dto);

    void delete(Long id);
}
