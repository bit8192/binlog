package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.base.entity.ISystemFile;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.utils.FileUtils;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import cn.bincker.web.blog.base.entity.LocalSystemFileImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

@Service
@ConditionalOnProperty(value = "binlog.files.type", havingValue = "Local")
public class LocalSystemFileFactoryImpl implements ISystemFileFactory {
    public static final String CACHE_KEY_DOWNLOAD_CODE = "DOWNLOAD-FILE-CODE-";
    private final ISystemCacheService systemCacheService;
    private final String basePath;

    public LocalSystemFileFactoryImpl(ISystemCacheService systemCacheService, @Value("${binlog.base-path}") String basePath) {
        this.systemCacheService = systemCacheService;
        this.basePath = basePath;
    }

    @Override
    public ISystemFile fromPath(String path) {
        return new LocalSystemFileImpl(path);
    }

    @Override
    public ISystemFile fromPath(String ...paths) {
        return fromPath(FileUtils.join(paths));
    }

    @Override
    public ISystemFile fromNetDiskFile(NetDiskFile netDiskFile) {
        return fromPath(netDiskFile.getPath());
    }

    @Override
    public String getDownloadUrl(HttpServletRequest request, NetDiskFile netDiskFile) {
        String code = generateDownloadKey();
        while (systemCacheService.containsKey(CACHE_KEY_DOWNLOAD_CODE + code)) code = generateDownloadKey();
        systemCacheService.put(CACHE_KEY_DOWNLOAD_CODE + code, "", Duration.ofMinutes(10));//十分钟有效
        return RequestUtils.getRequestBaseUrl(request) + basePath + "/net-disk-files/download/" + netDiskFile.getId() + "?code=" + code;
    }

    private String generateDownloadKey(){
        return Long.toHexString(System.nanoTime());
    }

    @Override
    public String getDownloadUrl(String path) {
        return null;
    }
}
