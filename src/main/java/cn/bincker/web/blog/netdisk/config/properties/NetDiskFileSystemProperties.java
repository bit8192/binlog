package cn.bincker.web.blog.netdisk.config.properties;

import cn.bincker.web.blog.netdisk.enumeration.FileSystemTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("netdisk")
@Component
@Data
public class NetDiskFileSystemProperties {
    private FileSystemTypeEnum type;
}
