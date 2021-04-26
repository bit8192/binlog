package cn.bincker.web.blog.security;

import cn.bincker.web.blog.security.machine.ApiBackgroundGenerator;
import cn.bincker.web.blog.security.machine.ChineseVerifyCode;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerifyCodeConfig {
    @Bean
    public IVerifyCode<?> verifyCode(){
        return new ChineseVerifyCode(
                350,
                350,
                2,
                4,
                10,
                90,
                28,
                20,
                new String[]{"宋体"},
                new ApiBackgroundGenerator()
        );
    }
}
