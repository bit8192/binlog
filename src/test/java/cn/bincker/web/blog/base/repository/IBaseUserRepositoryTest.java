package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.base.specification.BaseUserSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IBaseUserRepositoryTest {
    @Autowired
    private IBaseUserRepository baseUserRepository;

    @Test
    void name() {
        System.out.println(baseUserRepository.findAll(BaseUserSpecification.role(Role.RoleEnum.BLOGGER)).size());
    }
}
