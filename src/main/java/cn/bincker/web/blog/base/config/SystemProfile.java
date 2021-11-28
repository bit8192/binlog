package cn.bincker.web.blog.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("binlog")
@Data
public class SystemProfile {

    /**
     * 站点名称
     */
    private String name;

    /**
     * 是否开发环境
     */
    private Boolean isDev;

    /**
     * 展示版权
     */
    private String copyRight;

    /**
     * 展示ipc
     */
    private String icp;

    /**
     * 展示github
     */
    private String github;

    /**
     * 时间格式化
     */
    private String datetimeFormat;

    private String dateFormat;

    private String timeFormat;

    private String expression = "happy";

    /**
     * 是否允许注册用户
     */
    private Boolean allowRegister = true;

    /**
     * 记住我的加密密钥
     */
    private String rememberMeKey;

    /**
     * 接口前缀
     */
    private String apiPath;
}
