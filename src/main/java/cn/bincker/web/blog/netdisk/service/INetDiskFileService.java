package cn.bincker.web.blog.netdisk.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.service.vo.NetDiskFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface INetDiskFileService {
    /**
     * 创建目录, 这里的添加都是添加目录, 上传在上传文件部分
     */
    NetDiskFileVo createDirectory(NetDiskFileDto dto);

    /**
     * 上传文件
     * @param multipartFiles 文件列表
     * @param dto 文件属性
     */
    List<NetDiskFileVo> upload(Collection<MultipartFile> multipartFiles, NetDiskFileDto dto);

    /**
     * 删除, 包括删除子目录以及子文件, 只有所有者和创建者可以删除
     */
    void delete(Long id);

    /**
     * 修改
     */
    NetDiskFileVo save(NetDiskFileDto dto);

    /**
     * 列出用户根目录
     */
    List<NetDiskFileVo> listUserRootVo(Long userId);

    /**
     * 列出当前用户根目录
     */
    List<NetDiskFileVo> listCurrentUserRootVo();

    /**
     * 列出子节点
     */
    List<NetDiskFileVo> listChildrenVo(Long id);

    /**
     * 通过id查询
     */
    Optional<NetDiskFile> findById(Long id);

    /**
     * 检测读取权限
     */
    void checkReadPermission(BaseUser user, NetDiskFile netDiskFile);

    /**
     * 检测写入权限
     */
    void checkWritePermission(BaseUser user, NetDiskFile netDiskFile);
}
