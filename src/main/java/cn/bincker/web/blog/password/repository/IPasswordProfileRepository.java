package cn.bincker.web.blog.password.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.entity.PasswordProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPasswordProfileRepository extends JpaRepository<PasswordProfile, Long> {
    Optional<PasswordProfile> findByCreatedUser(BaseUser user);
}
