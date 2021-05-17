package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.netdisk.config.properties.NetDiskFileSystemProperties;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.exception.DeleteFileFailException;
import cn.bincker.web.blog.netdisk.exception.MakeDirectoryFailException;
import cn.bincker.web.blog.netdisk.exception.RenameFileFailException;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.netdisk.service.ISystemFile;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.FileUtils;
import cn.bincker.web.blog.utils.SystemResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NetDiskFileServiceImpl implements INetDiskFileService {
    private static final Logger log = LoggerFactory.getLogger(NetDiskFileServiceImpl.class);

    private final INetDiskFileRepository netDiskFileRepository;
    private final UserAuditingListener userAuditingListener;
    private final SystemResourceUtils systemResourceUtils;
    private final IUploadFileRepository uploadFileRepository;
    private final ISystemFileFactory systemFileFactory;
    private final NetDiskFileSystemProperties netDiskFileSystemProperties;
    private final IBaseUserRepository baseUserRepository;

    public NetDiskFileServiceImpl(INetDiskFileRepository netDiskFileRepository, UserAuditingListener userAuditingListener, SystemResourceUtils systemResourceUtils, IUploadFileRepository uploadFileRepository, ISystemFileFactory systemFileFactory, NetDiskFileSystemProperties netDiskFileSystemProperties, IBaseUserRepository baseUserRepository) {
        this.netDiskFileRepository = netDiskFileRepository;
        this.userAuditingListener = userAuditingListener;
        this.systemResourceUtils = systemResourceUtils;
        this.uploadFileRepository = uploadFileRepository;
        this.systemFileFactory = systemFileFactory;
        this.netDiskFileSystemProperties = netDiskFileSystemProperties;
        this.baseUserRepository = baseUserRepository;
    }

    @Override
    @Transactional
    public NetDiskFileVo createDirectory(NetDiskFileDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);

        var directoryName = dto.getName().replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE, "");

//        构建实体
        var target = new NetDiskFile();
        target.setName(dto.getName());
        target.setIsDirectory(true);

//        如果有父级，那么设置所有者，并设置路径
        ISystemFile targetPath;
        if(dto.getParentId() != null){
            var parent = netDiskFileRepository.findById(dto.getParentId())
                    .orElseThrow(()->new NotFoundException("父级目录不存在", "创建目录时父级目录不存在： NetDiskFile.id=" + dto.getParentId()));
//            判断是否有权限创建
            checkWritePermission(Optional.of(currentUser), parent);
            target.setPossessor(parent.getPossessor());
            targetPath = systemFileFactory.fromPath(parent.getPath() + File.separator + dto.getName());
            target.setPath(targetPath.getPath());
            target.setParent(parent);
            setParent(target, parent);
        }
//        否则路径是用户根路径，父级为空，所有者为自己
        else{
            targetPath = systemFileFactory.fromPath(systemResourceUtils.getUploadPath(currentUser.getUsername()).getPath(), directoryName);
            target.setPath(targetPath.getPath());
            target.setPossessor(currentUser);
        }

//            所有者才可以修改权限
        if(target.getPossessor().getId().equals(currentUser.getId())){
            setNetDiskFilePermission(dto, target);
        }

//        持久化
        netDiskFileRepository.save(target);
//        生成视图
        NetDiskFileVo vo = new NetDiskFileVo(target, 0L);

//        创建目录放在最后面执行
        if(!targetPath.exists() && !targetPath.mkdirs()) throw new MakeDirectoryFailException(targetPath);
        return vo;
    }

    /**
     * 设置权限
     */
    private void setNetDiskFilePermission(NetDiskFileDto dto, NetDiskFile target) {
        target.setEveryoneReadable(dto.getEveryoneReadable());
        target.setEveryoneWritable(dto.getEveryoneWritable());
        target.setReadableUserList(dto.getReadableUserList().stream().map(id -> {
            var user = new BaseUser();
            user.setId(id);
            return user;
        }).collect(Collectors.toSet()));
        target.setWritableUserList(dto.getWritableUserList().stream().map(id -> {
            var user = new BaseUser();
            user.setId(id);
            return user;
        }).collect(Collectors.toSet()));
    }

    @Override
    @Transactional
    public List<NetDiskFileVo> upload(Collection<MultipartFile> multipartFiles, NetDiskFileDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        String targetPath;
        Optional<NetDiskFile> parentOptional = dto.getParentId() == null ? Optional.empty() : netDiskFileRepository.findById(dto.getParentId());
        targetPath = parentOptional
                .map(NetDiskFile::getPath)
                .orElseGet(() -> systemResourceUtils.getUploadPath(currentUser.getUsername()).getPath());
        var result = new ArrayList<NetDiskFileVo>(multipartFiles.size());
        for (MultipartFile multipartFile : multipartFiles) {
            //构建uploadFile
            UploadFile uploadFile = new UploadFile();
            if(StringUtils.hasText(multipartFile.getOriginalFilename())){
                uploadFile.setName(multipartFile.getOriginalFilename().replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE, ""));
            }else{
                uploadFile.setName(UUID.randomUUID().toString());
            }
            uploadFile.setPath(targetPath + File.separator + uploadFile.getName());
            uploadFile.setStorageLocation(netDiskFileSystemProperties.getType());
            uploadFile.setSuffix(CommonUtils.getStringSuffix(uploadFile.getName(), "."));
            uploadFile.setMediaType(multipartFile.getContentType());
            uploadFile.setSize(multipartFile.getSize());
            uploadFile.setIsPublic(false);
            //写到文件, 并计算sha256
            var systemFile = systemFileFactory.fromPath(targetPath, uploadFile.getName());
            while (systemFile.exists()){
                systemFile = systemFileFactory.fromPath(targetPath, FileUtils.nextSerialFileName(systemFile.getName()));
            }
            try(var in = multipartFile.getInputStream(); var out = systemFile.getOutputStream()){
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                var buff = new byte[8192];
                int len;
                while ((len = in.read(buff)) > 0){
                    messageDigest.update(buff, 0, len);
                    out.write(buff, 0, len);
                }
                uploadFile.setSha256(CommonUtils.bytes2hex(messageDigest.digest()));
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            //持久化uploadFile
            uploadFileRepository.save(uploadFile);
            var netDiskFile = new NetDiskFile();
            netDiskFile.setName(uploadFile.getName());
            netDiskFile.setPath(uploadFile.getPath());
            if(parentOptional.isPresent()){
                var parent = parentOptional.get();
                netDiskFile.setParent(parent);
                netDiskFile.setPossessor(parent.getPossessor());
                setParent(netDiskFile, parent);
            }else{
                netDiskFile.setPossessor(currentUser);
            }
            netDiskFile.setIsDirectory(false);
            netDiskFile.setUploadFile(uploadFile);
            //如果是持有者，那么设置权限
            if(currentUser.getId().equals(netDiskFile.getPossessor().getId())){
                setNetDiskFilePermission(dto, netDiskFile);
            }
            //持久化
            netDiskFileRepository.save(netDiskFile);
            //填上用户数据, 因为这里的用户只有id, 查也查不出来（被缓存了吧，能想到的办法只有手动查）
            netDiskFile.setReadableUserList(new HashSet<>(baseUserRepository.findAllById(dto.getReadableUserList())));
            netDiskFile.setWritableUserList(new HashSet<>(baseUserRepository.findAllById(dto.getWritableUserList())));
            result.add(new NetDiskFileVo(netDiskFile));
        }
        return result;
    }

    /**
     * 设置父级
     */
    private void setParent(NetDiskFile netDiskFile, NetDiskFile parent) {
        var parents = new Long[parent.getParents().length + 1];
        if(parents.length > 1) System.arraycopy(parent.getParents(), 0, parents, 0, parent.getParents().length);
        parents[parents.length - 1] = parent.getId();
        netDiskFile.setParents(parents);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        //当前用户
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);

        //取出删除对象
        var target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);

        //权限判断
        checkDeletePermission(Optional.of(currentUser), target);

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
            try {
                var file = systemFileFactory.fromPath(uploadFile.getPath());
                if(!file.delete()) throw new DeleteFileFailException(file);
            }catch (Exception e){
                //出现任何异常都不进行回滚，因为即使回滚了也可能有一部分文件已经被删除了
                log.error("删除目录失败：path=" + uploadFile.getPath(), e);
            }
        }
//        删除所有目录, 从底部向上删除
        for (int i = allDirectory.size() - 1; i > -1; i--) {
            try {
                var file = systemFileFactory.fromPath(allDirectory.get(i).getPath());
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
    public NetDiskFileVo save(NetDiskFileDto dto) {
        //当前用户
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
//        更新对象
        var target = netDiskFileRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
//        权限判断
        checkUpdatePermission(Optional.of(currentUser), target);
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
            setNetDiskFilePermission(dto, target);
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
            if(!systemFileFactory.fromPath(oldPath).renameTo(target.getPath()))
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
    public List<NetDiskFileListVo> listUserRootVo(Long userId) {
        return netDiskFileRepository.listUserRootVo(userId);
    }

    @Override
    public List<NetDiskFileListVo> listCurrentUserRootVo() {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        return netDiskFileRepository.listUserRootVo(currentUser.getId());
    }

    @Override
    public List<NetDiskFileListVo> listChildrenVo(Long id) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);
        //检测权限
        checkReadPermission(Optional.of(currentUser), target);
        return netDiskFileRepository.listVoByParentId(id);
    }

    @Override
    public List<NetDiskFileListVo> findAllVoById(Long[] ids) {
        return netDiskFileRepository.findAllVoById(ids);
    }

    @Override
    public List<NetDiskFile> findAllById(Long[] ids) {
        return netDiskFileRepository.findAllById(Arrays.asList(ids));
    }

    @Override
    public Optional<NetDiskFile> findById(Long id) {
        return netDiskFileRepository.findById(id);
    }

    /**
     * 检测写入权限
     */
    @Override
    public void checkWritePermission(Optional<BaseUser> userOptional, NetDiskFile target){
        if (!target.getEveryoneWritable()) {//若不是所有人都可写入
            var user = userOptional.orElseThrow(UnauthorizedException::new);
            if(!target.getPossessor().getId().equals(user.getId())) {//若不是所有者创建
                var writableUserIds = netDiskFileRepository.getWritableUserIds(target.getId());
                if(writableUserIds.stream().noneMatch(uid -> uid.equals(user.getId())))
                    throw new ForbiddenException();
            }
        }
    }

    @Override
    public Optional<NetDiskFileVo> findVoById(Long id) {
        var userOptional = userAuditingListener.getCurrentAuditor();
        var result = netDiskFileRepository.findVoById(id);
        if(result.isEmpty()) return result;
        var vo = result.get();
        if(vo.getEveryoneReadable()){
            vo.setReadable(true);
        }else{
            if(userOptional.isEmpty()){
                vo.setReadable(false);
            }else{
                var user = userOptional.get();
                vo.setReadable(vo.getPossessor().getId().equals(user.getId()) || vo.getReadableUserList().stream().anyMatch(u->u.getId().equals(user.getId())));
            }
        }
        if(vo.getEveryoneWritable()){
            vo.setWritable(true);
        }else{
            if(userOptional.isEmpty()){
                vo.setWritable(false);
            }else{
                var user = userOptional.get();
                vo.setWritable(vo.getPossessor().getId().equals(user.getId()) || vo.getWritableUserList().stream().anyMatch(u->u.getId().equals(user.getId())));
            }
        }

        if(userOptional.isEmpty() || !userOptional.get().getId().equals(vo.getPossessor().getId())){
            vo.setEveryoneReadable(null);
            vo.setEveryoneWritable(null);
            vo.setReadableUserList(Collections.emptyList());
            vo.setWritableUserList(Collections.emptyList());
        }
        return result;
    }

    /**
     * 检测删除权限
     */
    private void checkDeletePermission(Optional<BaseUser> userOptional, NetDiskFile target) {
        checkWritePermission(userOptional, target);
    }

    /**
     * 检测更新权限
     */
    private void checkUpdatePermission(Optional<BaseUser> userOptional, NetDiskFile target) {
        checkWritePermission(userOptional, target);
    }

    /**
     * 检测读取权限
     */
    @Override
    public void checkReadPermission(Optional<BaseUser> userOptional, NetDiskFile target){
        if (!target.getEveryoneReadable()) {//若不是所有人都可读取
            var user = userOptional.orElseThrow(UnauthorizedException::new);
            if(!target.getPossessor().getId().equals(user.getId())) {//若不是所有者
                var readableUserList = netDiskFileRepository.getReadableUserIds(target.getId());
                if(readableUserList.stream().noneMatch(id -> id.equals(user.getId())))
                    throw new ForbiddenException();
            }
        }
    }
}
