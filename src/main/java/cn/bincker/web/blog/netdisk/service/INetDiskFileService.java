package cn.bincker.web.blog.netdisk.service;

import cn.bincker.web.blog.netdisk.service.dto.NetDiskFilePostDto;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFilePutDto;
import cn.bincker.web.blog.netdisk.service.vo.NetDiskFileVo;

import java.util.List;

public interface INetDiskFileService {
    /**
     * 添加, 这里的添加都是添加目录, 上传在上传文件部分
     */
    NetDiskFileVo add(NetDiskFilePostDto dto);

    /**
     * 删除, 包括删除子目录以及子文件, 只有所有者和创建者可以删除
     */
    void delete(Long id);

    /**
     * 修改
     */
    NetDiskFileVo save(NetDiskFilePutDto dto);

    /**
     * 列出用户根目录
     */
    List<NetDiskFileVo> listUserRoot(Long userId);

    /**
     * 列出当前用户根目录
     */
    List<NetDiskFileVo> listCurrentUserRoot();

    /**
     * 列出子节点
     */
    List<NetDiskFileVo> listChildren(Long id);
}
