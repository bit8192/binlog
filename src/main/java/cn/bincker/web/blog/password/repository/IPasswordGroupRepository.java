package cn.bincker.web.blog.password.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.entity.PasswordGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPasswordGroupRepository extends JpaRepository<PasswordGroup, Long> {
    Page<PasswordGroup> findAllByCreatedUserAndTitleLike(BaseUser user, String keywords, Pageable pageable);
}
