package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.entity.ISystemFile;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import cn.bincker.web.blog.netdisk.entity.LocalSystemFileImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

@Service
@ConditionalOnProperty(value = "netdisk.type", havingValue = "Local")
public class LocalSystemFileFactoryImpl implements ISystemFileFactory {
    public static final String CACHE_KEY_DOWNLOAD_CODE = "DOWNLOAD-FILE-CODE-";
    private final ISystemCacheService systemCacheService;
    private final String basePath;

    public LocalSystemFileFactoryImpl(ISystemCacheService systemCacheService, @Value("${system.base-path}") String basePath) {
        this.systemCacheService = systemCacheService;
        this.basePath = basePath;
    }

    @Override
    public ISystemFile fromPath(String child) {
        return new LocalSystemFileImpl(child);
    }

    @Override
    public ISystemFile fromPath(String path, String child) {
        return new LocalSystemFileImpl(path, child);
    }

    @Override
    public ISystemFile fromNetDiskFile(NetDiskFile netDiskFile) {
        return new LocalSystemFileImpl(netDiskFile);
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
}
