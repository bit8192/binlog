package cn.bincker.web.blog.netdisk.service;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.service.impl.LocalSystemFileImpl;

import java.util.Optional;

public interface ISystemFileFactory {
    ISystemFile fromPath(String path);
    ISystemFile fromPath(String path, String child);
    ISystemFile fromNetDiskFile(NetDiskFile netDiskFile);
    Optional<LocalSystemFileImpl> fromNetDiskFileId(Long id);
}
