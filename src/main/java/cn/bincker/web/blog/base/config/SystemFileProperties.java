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
     * 默认存储位置
     */
    private FileSystemTypeEnum defaultStoreType = FileSystemTypeEnum.LOCAL;
    /**
     * 储存文件路径，包括云端路径
     */
    private String location = "upload-file";
    /**
     * 缩略图缓存路径
     */
    private String imageCacheLocation = "image-cache";
    /**
     * 可使用外链地址列表
     */
    private String[] allowReferer = new String[]{"*.bincker.cn"};
    /**
     * 外链是否允许空引用
     */
    private Boolean allowEmptyReferer = true;
    /**
     * 表情图片存储类型
     */
    private FileSystemTypeEnum expressionStoreType = FileSystemTypeEnum.LOCAL;
    /**
     * 表情图片存储路径
     */
    private String expressionStoreLocation = ".expression";
    /**
     * 阿里云OSS
     */
    private AliyunOssProperties aliyunOss;
    /**
     * 下载链接有效时长 毫秒
     */
    private long downloadUrlExpirationTime = 60000 * 60;
}
