package cn.bincker.web.blog.password.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.dto.PasswordInfoDto;
import cn.bincker.web.blog.password.entity.PasswordInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPasswordInfoService {
    Page<PasswordInfo> page(BaseUser user, Long passwordGroupId, String keywords, Pageable pageable);

    PasswordInfo add(PasswordInfoDto dto);

    PasswordInfo update(PasswordInfoDto dto);

    void delete(Long id);

    void updateUseTimes(Long id);
}
