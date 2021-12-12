package cn.bincker.web.blog.base.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOssConfig {
    @Bean
    @ConditionalOnProperty({
            "binlog.files.aliyun-oss.endpoint",
            "binlog.files.aliyun-oss.access-key-id",
            "binlog.files.aliyun-oss.access-key-secret",
            "binlog.files.aliyun-oss.bucket-name",
    })
    public OSS aliyunOss(SystemFileProperties systemFileProperties){
        var config = systemFileProperties.getAliyunOss();
        return new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKeyId(), config.getAccessKeySecret());
    }
}
