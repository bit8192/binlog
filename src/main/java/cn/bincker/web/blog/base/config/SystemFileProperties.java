package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.enumeration.FileSystemTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("binlog.files")
@Component
@Data
public class SystemFileProperties {
    /**
     * 储存文件路径
     */
    private String location = "";
    private FileSystemTypeEnum type;
    private String[] allowReferer;
    private Boolean allowEmptyReferer;
}
