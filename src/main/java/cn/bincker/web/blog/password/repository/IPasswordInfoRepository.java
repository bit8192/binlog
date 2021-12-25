package cn.bincker.web.blog.password.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.entity.PasswordInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPasswordInfoRepository extends JpaRepository<PasswordInfo, Long> {
    Page<PasswordInfo> findAllByPasswordGroupIdAndCreatedUserAndTitleLike(Long passwordGroupId, BaseUser user, String keywords, Pageable pageable);
}
