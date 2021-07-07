package cn.bincker.web.blog.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("system")
@Data
public class SystemProfile {
    /**
     * 站点名称
     */
    private String name;
    /**
     * 展示版权
     */
    private String copyRight;
    /**
     * 展示ipc
     */
    private String ipc;
    /**
     * 展示github
     */
    private String github;
    /**
     * url前缀
     */
    private String basePath;
    /**
     * 时间格式化
     */
    private String datetimeFormat;
    private String dateFormat;
    private String timeFormat;
}
