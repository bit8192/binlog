package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.exception.DeleteFileFailException;
import cn.bincker.web.blog.netdisk.exception.MakeDirectoryFailException;
import cn.bincker.web.blog.netdisk.exception.RenameFileFailException;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFilePostDto;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFilePutDto;
import cn.bincker.web.blog.netdisk.service.vo.NetDiskFileVo;
import cn.bincker.web.blog.utils.SystemResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NetDiskFileServiceImpl implements INetDiskFileService {
    private static final Logger log = LoggerFactory.getLogger(NetDiskFileServiceImpl.class);

    private final INetDiskFileRepository netDiskFileRepository;
    private final UserAuditingListener userAuditingListener;
    private final SystemResourceUtils systemResourceUtils;
    private final IUploadFileRepository uploadFileRepository;

    public NetDiskFileServiceImpl(INetDiskFileRepository netDiskFileRepository, UserAuditingListener userAuditingListener, SystemResourceUtils systemResourceUtils, IUploadFileRepository uploadFileRepository) {
        this.netDiskFileRepository = netDiskFileRepository;
        this.userAuditingListener = userAuditingListener;
        this.systemResourceUtils = systemResourceUtils;
        this.uploadFileRepository = uploadFileRepository;
    }

    @Override
    @Transactional
    public NetDiskFileVo add(NetDiskFilePostDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);

        var directoryName = dto.getName().replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR, "");

//        构建实体
        var target = new NetDiskFile();
        target.setName(dto.getName());
        target.setIsDirectory(true);

//        如果有父级，那么设置所有者，并设置路径
        File targetPath;
        if(dto.getParentId() != null){
            var parent = netDiskFileRepository.findById(dto.getParentId())
                    .orElseThrow(()->new NotFoundException("父级目录不存在", "创建目录时父级目录不存在： NetDiskFile.id=" + dto.getParentId()));
//            判断是否有权限创建
            checkWritePermission(currentUser, parent);
            target.setPossessor(parent.getPossessor());
            targetPath = new File(target.getPath());
            target.setPath(targetPath.getPath());
        }
//        否则路径是用户根路径，父级为空，所有者为自己
        else{
            targetPath = new File(systemResourceUtils.getUploadPath(currentUser.getUsername()), directoryName);
            target.setPath(targetPath.getPath());
            target.setPossessor(currentUser);
        }

//            所有者才可以修改权限
        if(target.getPossessor().getId().equals(currentUser.getId())){
            if(dto.getEveryoneReadable() != null) target.setEveryoneReadable(dto.getEveryoneReadable());
            if(dto.getEveryoneWritable() != null) target.setEveryoneWritable(dto.getEveryoneWritable());
            if(dto.getReadableUserList() != null)
                target.setReadableUserList(dto.getReadableUserList().stream().map(id->{
                    var user = new BaseUser();
                    user.setId(id);
                    return user;
                }).collect(Collectors.toList()));
            if(dto.getWritableUserList() != null)
                target.setWritableUserList(dto.getWritableUserList().stream().map(id->{
                    var user = new BaseUser();
                    user.setId(id);
                    return user;
                }).collect(Collectors.toList()));
        }

//        持久化
        netDiskFileRepository.save(target);
//        生成视图
        NetDiskFileVo vo = new NetDiskFileVo();
        vo.setId(target.getId());
        vo.setName(target.getName());
        vo.setIsDirectory(true);

//        创建目录放在最后面执行
        if(!targetPath.exists() && !targetPath.mkdirs()) throw new MakeDirectoryFailException(targetPath);
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        //当前用户
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);

        //取出删除对象
        var target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);

        //权限判断
        checkDeletePermission(currentUser, target);

        //列出要删除的子目录和文件
        List<NetDiskFile> allFileAndDirectory = listAllChildren(target);
        List<NetDiskFile> allFile = new ArrayList<>(), allDirectory = new ArrayList<>();
        for (NetDiskFile netDiskFile : allFileAndDirectory) {
            if(netDiskFile.getIsDirectory()){
                allDirectory.add(netDiskFile);
            }else{
                allFile.add(netDiskFile);
            }
        }
        List<UploadFile> uploadFiles = uploadFileRepository.findAllById(
                allFile.stream().map(f->{
                    var uploadFile = f.getUploadFile();
                    if(uploadFile == null) return null;
                    return uploadFile.getId();
                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
//        删除上传的文件记录
        uploadFileRepository.deleteAll(uploadFiles);
//        删除所有文件记录
        netDiskFileRepository.deleteAll(allFileAndDirectory);
//        删除上传文件实体
        for (UploadFile uploadFile : uploadFiles) {
            switch (uploadFile.getStorageLocation()) {
                case LOCAL -> {
                    try {
                        var file = new File(uploadFile.getPath());
                        if(!file.delete()) throw new DeleteFileFailException(file);
                    }catch (Exception e){
                        //出现任何异常都不进行回滚，因为即使回滚了也可能有一部分文件已经被删除了
                        log.error("删除目录失败：path=" + uploadFile.getPath(), e);
                    }
                }
                case ALI_ -> {
//                    TODO 删除云端文件
                }
            }
        }
//        删除所有目录, 从底部向上删除
        for (int i = allDirectory.size() - 1; i > -1; i--) {
            try {
                var file = new File(allDirectory.get(i).getPath());
                if(!file.delete()) throw new DeleteFileFailException(file);
            }catch (Exception e){
                //出现任何异常都不进行回滚，因为即使回滚了也可能有一部分文件已经被删除了
                log.error("删除目录失败：path=" + allDirectory.get(i).getPath());
            }
        }
    }

    /**
     * 列出所有子节点成一个数组(包括当前节点)
     */
    private List<NetDiskFile> listAllChildren(NetDiskFile target){
        List<NetDiskFile> result = new ArrayList<>();
        result.add(target);
        for (int i = 0; i < result.size(); i++) {
//            查出当前节点的子节点加入尾端
            result.addAll(netDiskFileRepository.findByParent(result.get(i).getId()));
        }
        return result;
    }

    @Override
    @Transactional
    public NetDiskFileVo save(NetDiskFilePutDto dto) {
        //当前用户
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
//        更新对象
        var target = netDiskFileRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
//        权限判断
        checkUpdatePermission(currentUser, target);
//        移动操作
        var oldPath = target.getPath();
        if(dto.getParentId() != null || target.getParent() != null){//如果传入parent不为空，或者传入parent未空但原有parent不为空
            if(dto.getParentId() == null){//传入parent为空则移动到根目录
                target.setParent(null);
                target.setPath(systemResourceUtils.getUploadPath(currentUser.getUsername() + File.separator + dto.getName()).getPath());
            }else{
                var parent = netDiskFileRepository.findById(dto.getParentId())
                        .orElseThrow(()->new NotFoundException("父级不存在", "移动操作失败父级节点不存在：id=" + dto.getParentId()));
                target.setParent(parent);
                target.setPath(new File(new File(parent.getPath()), dto.getName()).getPath());
            }
        }
//        重命名
        if(!dto.getName().equals(target.getName())){
            target.setName(dto.getName());
            target.setPath(new File(new File(target.getPath()).getParentFile(), target.getName()).getPath());
        }
//        只有所有者才能修改权限
        if(target.getPossessor().getId().equals(currentUser.getId())) {
            if (dto.getEveryoneReadable() == null) {
                target.setEveryoneReadable(false);
            } else {
                target.setEveryoneReadable(dto.getEveryoneReadable());
            }
            if (dto.getEveryoneWritable() == null) {
                target.setEveryoneWritable(false);
            } else {
                target.setEveryoneWritable(dto.getEveryoneWritable());
            }
            if (dto.getWritableUserList() != null)
                target.setReadableUserList(dto.getReadableUserList().stream().map(id -> {
                    var user = new BaseUser();
                    user.setId(id);
                    return user;
                }).collect(Collectors.toList()));
            if (dto.getReadableUserList() != null)
                target.setWritableUserList(dto.getWritableUserList().stream().map(id -> {
                    var user = new BaseUser();
                    user.setId(id);
                    return user;
                }).collect(Collectors.toList()));
        }

//        持久化
        netDiskFileRepository.save(target);
//        修改uploadFile
        if(!target.getIsDirectory()){
            var uploadFile = uploadFileRepository.findById(target.getUploadFile().getId())
                    .orElseThrow(()->new NotFoundException("文件不存在", "uploadFile不存在: netDiskFileId=" + target.getId() + "\tuploadFileId=" + target.getUploadFile().getId()));
            uploadFile.setName(dto.getName());
            uploadFile.setPath(target.getPath());
            uploadFileRepository.save(uploadFile);
        }
//        执行实体文件操作
        if(!oldPath.equals(target.getPath())){
            if(!new File(oldPath).renameTo(new File(target.getPath())))
                throw new RenameFileFailException(oldPath, target.getPath());
        }
//        vo
        var vo = new NetDiskFileVo();
        vo.setId(target.getId());
        vo.setName(target.getName());
        vo.setIsDirectory(target.getIsDirectory());
        return vo;
    }

    @Override
    public List<NetDiskFileVo> listUserRoot(Long userId) {
        return netDiskFileRepository.listUserRootVo(userId);
    }

    @Override
    public List<NetDiskFileVo> listCurrentUserRoot() {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        return netDiskFileRepository.listUserRootVo(currentUser.getId());
    }

    @Override
    public List<NetDiskFileVo> listChildren(Long id) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);
        //检测权限
        checkReadPermission(currentUser, target);
        return netDiskFileRepository.listVoByParentId(id);
    }

    /**
     * 检测写入权限
     */
    private void checkWritePermission(BaseUser user, NetDiskFile target){
        if(!target.getPossessor().getId().equals(user.getId())) {//若不是所有者创建
            if (!target.getEveryoneWritable()) {//若不是所有人都可写入
                if(target.getWritableUserList().stream().noneMatch(u -> u.getId().equals(user.getId())))
                    throw new ForbiddenException();
            }
        }
    }

    /**
     * 检测删除权限
     */
    private void checkDeletePermission(BaseUser currentUser, NetDiskFile target) {
        if (!target.getPossessor().getId().equals(currentUser.getId()) && !target.getCreatedUser().getId().equals(currentUser.getId()))
            throw new ForbiddenException();
    }

    /**
     * 检测更新权限
     */
    private void checkUpdatePermission(BaseUser currentUser, NetDiskFile target) {
        if (!target.getPossessor().getId().equals(currentUser.getId()) && !target.getCreatedUser().getId().equals(currentUser.getId()))
            throw new ForbiddenException();
    }

    /**
     * 检测读取权限
     */
    private void checkReadPermission(BaseUser user, NetDiskFile target){
        if(!target.getPossessor().getId().equals(user.getId())) {//若不是所有者
            if (!target.getEveryoneReadable()) {//若不是所有人都可读取
                if(target.getReadableUserList().stream().noneMatch(u -> u.getId().equals(user.getId())))
                    throw new ForbiddenException();
            }
        }
    }
}
