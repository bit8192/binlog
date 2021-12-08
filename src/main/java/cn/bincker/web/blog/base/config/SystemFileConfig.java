package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.enumeration.FileSystemTypeEnum;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Collections;

@Configuration
public class SystemFileConfig {
    @Autowired
    private void mkdir(SystemFileProperties systemFileProperties, ISystemFileFactory systemFileFactory){
        var baseLocation = systemFileFactory.fromPath(Collections.singleton(FileSystemTypeEnum.LOCAL));
        if(!baseLocation.exists() && !baseLocation.mkdirs()) throw new RuntimeException("无法创建本地文件夹: " + baseLocation.getPath());

        //创建阿里云OSS根路径
        var aliyunOssProperties = systemFileProperties.getAliyunOss();
        if(aliyunOssProperties != null && StringUtils.hasText(aliyunOssProperties.getLocation())) {
            var aliyunLocation = systemFileFactory.fromPath(Collections.singleton(FileSystemTypeEnum.ALI_OSS));
            if(!aliyunLocation.exists() && !aliyunLocation.mkdirs()) throw new RuntimeException("无法创建阿里云OSS文件夹：" + aliyunOssProperties.getLocation());
        }

        var imageCacheLocation = new File(systemFileProperties.getImageCacheLocation());
        if(!imageCacheLocation.exists() && !imageCacheLocation.mkdirs()) throw new RuntimeException("无法创建本地图片缓存文件夹: " + imageCacheLocation.getPath());
    }

    @Autowired
    private void predicate(SystemFileProperties systemFileProperties){
        if(systemFileProperties.getAliyunOss() == null){
            if(systemFileProperties.getExpressionStoreType() == FileSystemTypeEnum.ALI_OSS)
                throw new RuntimeException("binlog.files.expression-store-type配置错误: 请配置binlog.files.aliyun-oss后再使用ALI_OSS");
        }
    }

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
