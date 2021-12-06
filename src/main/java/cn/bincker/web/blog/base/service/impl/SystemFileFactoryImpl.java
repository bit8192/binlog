package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.config.AliyunOssProperties;
import cn.bincker.web.blog.base.config.SystemFileProperties;
import cn.bincker.web.blog.base.config.SystemProfile;
import cn.bincker.web.blog.base.entity.AliyunOssSystemFileImpl;
import cn.bincker.web.blog.base.entity.CompositeSystemFile;
import cn.bincker.web.blog.base.enumeration.FileSystemTypeEnum;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.base.entity.ISystemFile;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.utils.FileUtils;
import cn.bincker.web.blog.utils.RequestUtils;
import com.aliyun.oss.OSS;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import cn.bincker.web.blog.base.entity.LocalSystemFileImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemFileFactoryImpl implements ISystemFileFactory {
    public static final String CACHE_KEY_DOWNLOAD_CODE = "DOWNLOAD-FILE-CODE-";
    private final ISystemCacheService systemCacheService;
    private final SystemProfile systemProfile;
    private final Optional<OSS> aliyunOss;
    private final SystemFileProperties systemFileProperties;
    private final AliyunOssProperties aliyunOssProperties;

    public SystemFileFactoryImpl(
            ISystemCacheService systemCacheService,
            SystemProfile systemProfile,
            Optional<OSS> aliyunOss,
            SystemFileProperties systemFileProperties
    ) {
        this.systemCacheService = systemCacheService;
        this.systemProfile = systemProfile;
        this.aliyunOss = aliyunOss;
        this.systemFileProperties = systemFileProperties;
        this.aliyunOssProperties = systemFileProperties.getAliyunOss();
    }

    private ISystemFile buildFile(FileSystemTypeEnum fileType, String path){
        switch (fileType){
            case LOCAL -> {
                return new LocalSystemFileImpl(systemFileProperties.getLocation(), path);
            }
            case ALI_OSS -> {
                return new AliyunOssSystemFileImpl(aliyunOss.orElseThrow(), aliyunOssProperties.getBucketName(), aliyunOssProperties.getLocation(), path);
            }
        }
        throw new IllegalArgumentException("未知的文件类型：" + fileType);
    }

    @Override
    public ISystemFile fromPath(@NonNull Set<FileSystemTypeEnum> fileSystemTypeEnumSet, String path) {
        if(fileSystemTypeEnumSet.size() == 1){
            return buildFile(fileSystemTypeEnumSet.iterator().next(), path);
        }else{
            List<ISystemFile> files = fileSystemTypeEnumSet.stream().map(t->buildFile(t, path)).collect(Collectors.toList());
            return new CompositeSystemFile(files);
        }
    }

    @Override
    public ISystemFile fromPath(FileSystemTypeEnum expressionStoreType, String path) {
        return buildFile(expressionStoreType, path);
    }

    @Override
    public ISystemFile fromPath(FileSystemTypeEnum fileSystemTypeEnum, String... paths) {
        return buildFile(fileSystemTypeEnum, FileUtils.join(paths));
    }

    @Override
    public ISystemFile fromPath(@NonNull Set<FileSystemTypeEnum> fileSystemTypeEnumSet, String ...paths) {
        return fromPath(fileSystemTypeEnumSet, FileUtils.join(paths));
    }

    @Override
    public ISystemFile fromNetDiskFile(@NonNull NetDiskFile netDiskFile) {
        return fromPath(netDiskFile.getFileSystemTypeSet(), netDiskFile.getPath());
    }

    @Override
    public ISystemFile fromNetDiskFile(@NonNull NetDiskFile netDiskFile, boolean isDirectory) {
        if(isDirectory){
            return fromPath(netDiskFile.getFileSystemTypeSet(), netDiskFile.getPath() + "/");
        }else{
            return fromPath(netDiskFile.getFileSystemTypeSet(), netDiskFile.getPath());
        }
    }

    @Override
    public String getDownloadUrl(@NonNull HttpServletRequest request, @NonNull NetDiskFile netDiskFile) {
        if(netDiskFile.getFileSystemTypeSet().size() > 1) throw new SystemException("不支持同时操作多个数据源");
        if(netDiskFile.getFileSystemTypeSet().isEmpty()) throw new SystemException("文件存储位置错误");
        return getDownloadUrl(request, netDiskFile.getFileSystemTypeSet().iterator().next(), netDiskFile.getPath());
    }

    @Override
    public String getDownloadUrl(HttpServletRequest request, FileSystemTypeEnum fileType, String path) {
        switch (fileType){
            case ALI_OSS -> {
                return aliyunOss.map(oss -> {
                    var expirationDate = new Date(System.currentTimeMillis() + systemFileProperties.getDownloadUrlExpirationTime());
                    var url = oss.generatePresignedUrl(aliyunOssProperties.getBucketName(), path, expirationDate);
                    return url.toString();
                }).orElseThrow();
            }
            case LOCAL -> {
                String code = generateDownloadKey();
                while (systemCacheService.containsKey(CACHE_KEY_DOWNLOAD_CODE + code)) code = generateDownloadKey();
                systemCacheService.put(CACHE_KEY_DOWNLOAD_CODE + code, "", systemFileProperties.getDownloadUrlExpirationTime());//十分钟有效
                return RequestUtils.getRequestBaseUrl(request) + systemProfile.getApiPath() + "/net-disk-files/download/" + path + "?code=" + code;
            }
        }
        throw new SystemException("未知的存储位置：" + fileType);
    }

    private String generateDownloadKey(){
        return Long.toHexString(System.nanoTime());
    }

}
