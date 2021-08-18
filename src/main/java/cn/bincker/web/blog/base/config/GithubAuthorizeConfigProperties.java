package cn.bincker.web.blog.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.Proxy;

@ConfigurationProperties("binlog.oauth2.github")
@Component
@Data
public class GithubAuthorizeConfigProperties {
    private boolean use = false;
    private String clientId;
    private String clientSecret;
    private boolean enableProxy = false;
    private Proxy.Type proxyType = Proxy.Type.SOCKS;
    private String proxyHost = "127.0.0.1";
    private int proxyPort = 1080;
}
