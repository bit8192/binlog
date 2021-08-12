package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IBaseUserRepository extends JpaRepository<BaseUser, Long>, JpaSpecificationExecutor<BaseUser> {
    Optional<BaseUser> findByUsername(String username);

    List<BaseUser> findAllByUsernameIn(Iterable<String> usernameSet);

    Optional<BaseUser> findByQqOpenId(String openId);

    Long countByCreatedDateBetween(Date start, Date end);
}
