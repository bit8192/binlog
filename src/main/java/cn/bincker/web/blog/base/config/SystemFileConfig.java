package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.service.ISystemFileFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class SystemFileConfig {
    private final ISystemFileFactory systemFileFactory;
    private final SystemFileProperties systemFileProperties;

    public SystemFileConfig(ISystemFileFactory systemFileFactory, SystemFileProperties systemFileProperties) {
        this.systemFileFactory = systemFileFactory;
        this.systemFileProperties = systemFileProperties;
    }

    @PostConstruct
    private void mkdir(){
        var baseLocation = systemFileFactory.fromPath(systemFileProperties.getLocation());
        if(!baseLocation.exists() && !baseLocation.mkdirs()) throw new RuntimeException("无法创建(云端/本地)文件夹: " + baseLocation.getPath());

        var imageCacheLocation = new File(systemFileProperties.getImageCacheLocation());
        if(!imageCacheLocation.exists() && !imageCacheLocation.mkdirs()) throw new RuntimeException("无法创建本地图片缓存文件夹: " + imageCacheLocation.getPath());
    }
}
