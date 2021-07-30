package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.ISystemFile;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;

import javax.servlet.http.HttpServletRequest;

public interface ISystemFileFactory {
    ISystemFile fromPath(String path);

    ISystemFile fromPath(String ...paths);

    ISystemFile fromNetDiskFile(NetDiskFile netDiskFile);

    String getDownloadUrl(HttpServletRequest request, NetDiskFile netDiskFile);

    String getDownloadUrl(String path);
}
