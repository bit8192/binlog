package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "users", collectionResourceRel = "users", itemResourceRel = "user")
public interface IBaseUserRepository extends JpaRepository<BaseUser, Long> {
    Optional<BaseUser> findByUsername(String username);
}
