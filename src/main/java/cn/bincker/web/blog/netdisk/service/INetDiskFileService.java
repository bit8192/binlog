package cn.bincker.web.blog.netdisk.service;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
     * 列出子节点
     * @param user 列出的文件将计算该用户的权限，若参数id为空，则列出该用户的根目录，可为空
     * @param id 将要列出该项目的子节点
     * @param isDirectory 是否是文件夹，为空则都列出
     * @param mediaType 对mediaType进行like查询
     * @param suffix 文件后缀限制
     * @param sort 排序
     */
    List<NetDiskFileListVo> listChildrenVo(@Nullable BaseUser user, @Nullable Long id, @Nullable Boolean isDirectory,@Nullable String mediaType,@Nullable String suffix,@Nullable Sort sort);

    /**
     * 通过id列出所有对象
     * @param ids
     */
    List<NetDiskFile> findAllById(List<Long> ids);

    /**
     * 通过id查询
     */
    Optional<NetDiskFile> findById(Long id);

    /**
     * 检测读取权限
     */
    void checkReadPermission(@Nullable BaseUser user, NetDiskFile netDiskFile);

    /**
     * 检测写入权限
     */
    void checkWritePermission(@Nullable BaseUser user, NetDiskFile netDiskFile);

    /**
     * 通过id查找vo
     */
    Optional<NetDiskFileVo> findVoById(Long id);

    /**
     * 获取下载链接
     */
    ValueVo<String> getDownloadUrl(HttpServletRequest request, Long id, BaseUser user);
}
