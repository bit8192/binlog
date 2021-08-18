package cn.bincker.web.blog.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("binlog.oauth2.qq")
@Data
public class QQAuthorizeConfigProperties {
    private boolean use = false;
    private String appId;
    private String appKey;
}
