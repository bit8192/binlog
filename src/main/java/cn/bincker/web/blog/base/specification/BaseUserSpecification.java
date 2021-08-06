package cn.bincker.web.blog.base.specification;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import org.springframework.data.jpa.domain.Specification;

public class BaseUserSpecification {
    public static Specification<BaseUser> role(Role.RoleEnum role){
        return (root, query, cb) -> cb.like(root.get("roles").as(String.class), "%" + role + "%");
    }
}
