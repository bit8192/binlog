package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.ISystemFile;
import cn.bincker.web.blog.base.enumeration.FileSystemTypeEnum;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import org.springframework.lang.NonNull;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public interface ISystemFileFactory {
    ISystemFile fromPath(@NonNull Set<FileSystemTypeEnum> fileSystemTypeEnumSet, String path);

    ISystemFile fromPath(@NonNull FileSystemTypeEnum expressionStoreType, String path);

    ISystemFile fromPath(@NonNull Set<FileSystemTypeEnum> fileSystemTypeEnumSet, String ...paths);

    ISystemFile fromPath(@NonNull FileSystemTypeEnum fileSystemTypeEnum, String ...paths);

    ISystemFile fromNetDiskFile(@NonNull NetDiskFile netDiskFile);

    ISystemFile fromNetDiskFile(@NonNull NetDiskFile netDiskFile, boolean isDirectory);

    String getDownloadUrl(@NonNull HttpServletRequest request, @NonNull NetDiskFile netDiskFile);

    String getDownloadUrl(@NonNull HttpServletRequest request, @NonNull FileSystemTypeEnum fileType, @NonNull String path);
}
