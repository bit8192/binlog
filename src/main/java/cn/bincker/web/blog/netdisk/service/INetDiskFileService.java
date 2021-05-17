package cn.bincker.web.blog.netdisk.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
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
     * @return
     */
    List<NetDiskFileListVo> listUserRootVo(Long userId);

    /**
     * 列出当前用户根目录
     * @return
     */
    List<NetDiskFileListVo> listCurrentUserRootVo();

    /**
     * 列出子节点
     * @return
     */
    List<NetDiskFileListVo> listChildrenVo(Long id);

    /**
     * 通过id列出所有vo
     * @return
     */
    List<NetDiskFileListVo> findAllVoById(Long[] ids);

    /**
     * 通过id列出所有对象
     */
    List<NetDiskFile> findAllById(Long[] ids);

    /**
     * 通过id查询
     */
    Optional<NetDiskFile> findById(Long id);

    /**
     * 检测读取权限
     */
    void checkReadPermission(Optional<BaseUser> user, NetDiskFile netDiskFile);

    /**
     * 检测写入权限
     */
    void checkWritePermission(Optional<BaseUser> user, NetDiskFile netDiskFile);

    /**
     * 通过id查找vo
     */
    Optional<NetDiskFileVo> findVoById(Long id);
}
