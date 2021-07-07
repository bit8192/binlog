package cn.bincker.web.blog.security.config;

import cn.bincker.web.blog.security.machine.ChineseVerifyCode;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import cn.bincker.web.blog.security.machine.LocalBackgroundGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerifyCodeConfig {
    @Bean
    public IVerifyCode<?> verifyCode(VerifyCodeConfigProperties properties){
        if(properties.getImagePath() == null){
            throw new RuntimeException("请配置验证码图片：system.verify-code.image-path");
        }
        return new ChineseVerifyCode(
                properties.getWidth(),
                properties.getHeight(),
                properties.getMinCharNumber(),
                properties.getMaxCharNumber(),
                properties.getPadding(),
                properties.getRotateLimit(),
                properties.getFontSize(),
                properties.getTitleFontSize(),
                properties.getFonts(),
                new LocalBackgroundGenerator(properties.getImagePath())
        );
    }
}
