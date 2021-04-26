package cn.bincker.web.blog.base.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("system")
@Data
public class SystemProfile {
    private String name;
    private String copyRight;
    private String ipc;
    private String github;
}
