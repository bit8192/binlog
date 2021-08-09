package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.service.IIpAddressQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Ip138QueryServiceImplTest {
    @Autowired
    private IIpAddressQueryService ipAddressQueryService;

    @Test
    void query() {
        var result = ipAddressQueryService.query("0.0.0.0");
        assertTrue(result.isPresent());
        System.out.println(result.get().getAddress());
    }
}
