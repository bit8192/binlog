package cn.bincker.web.blog.netdisk.service;

import cn.bincker.web.blog.netdisk.entity.ISystemFile;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;

import javax.servlet.http.HttpServletRequest;

public interface ISystemFileFactory {
    ISystemFile fromPath(String path);
    ISystemFile fromPath(String path, String child);
    ISystemFile fromNetDiskFile(NetDiskFile netDiskFile);
    String getDownloadUrl(HttpServletRequest request, NetDiskFile netDiskFile);
}
