package cn.bincker.web.blog.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConfigurationProperties("system.verify-code")
@Data
public class VerifyCodeConfigProperties {
    private int width = 350;
    private int height = 350;
    private int minCharNumber = 2;
    private int maxCharNumber = 4;
    private int padding = 10;
    /**
     * 最大旋转角度（角度制）
     */
    private int rotateLimit = 90;
    private int fontSize = 28;
    private int titleFontSize = 20;
    private String[] fonts = new String[]{"宋体"};
    private File imagePath;
}
