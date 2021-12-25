package cn.bincker.web.blog.password.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.dto.PasswordProfileDto;
import cn.bincker.web.blog.password.entity.PasswordProfile;

public interface IPasswordProfileService {
    PasswordProfile getByUser(BaseUser user);

    PasswordProfile update(BaseUser user, PasswordProfileDto Dto);
}
