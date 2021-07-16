package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.NotImplementedException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.EntityLongValueVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.netdisk.config.properties.NetDiskFileSystemProperties;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.exception.DeleteFileFailException;
import cn.bincker.web.blog.netdisk.exception.MakeDirectoryFailException;
import cn.bincker.web.blog.netdisk.exception.RenameFileFailException;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.netdisk.entity.ISystemFile;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.specification.NetDiskFileSpecification;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.FileUtils;
import cn.bincker.web.blog.utils.SystemResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
    private final ISystemFileFactory systemFileFactory;
    private final NetDiskFileSystemProperties netDiskFileSystemProperties;
    private final IBaseUserRepository baseUserRepository;

    public NetDiskFileServiceImpl(
            INetDiskFileRepository netDiskFileRepository,
            UserAuditingListener userAuditingListener,
            SystemResourceUtils systemResourceUtils,
            ISystemFileFactory systemFileFactory,
            NetDiskFileSystemProperties netDiskFileSystemProperties,
            IBaseUserRepository baseUserRepository
    ) {
        this.netDiskFileRepository = netDiskFileRepository;
        this.userAuditingListener = userAuditingListener;
        this.systemResourceUtils = systemResourceUtils;
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
            checkWritePermission(currentUser, parent);
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
        NetDiskFileVo vo = new NetDiskFileVo(target);

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
            var netDiskFile = new NetDiskFile();
            if(StringUtils.hasText(multipartFile.getOriginalFilename())){
                netDiskFile.setName(multipartFile.getOriginalFilename().replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE, ""));
            }else{
                netDiskFile.setName(UUID.randomUUID().toString());
            }
            netDiskFile.setPath(targetPath + File.separator + netDiskFile.getName());
            netDiskFile.setStorageLocation(netDiskFileSystemProperties.getType());
            netDiskFile.setSuffix(CommonUtils.getStringSuffix(netDiskFile.getName(), "."));//后缀全用小写，方便查询
            netDiskFile.setMediaType(multipartFile.getContentType());
            netDiskFile.setSize(multipartFile.getSize());
            //写到文件, 并计算sha256
            var systemFile = systemFileFactory.fromPath(targetPath, netDiskFile.getName());
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
                netDiskFile.setSha256(CommonUtils.bytes2hex(messageDigest.digest()));
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if(parentOptional.isPresent()){
                var parent = parentOptional.get();
                netDiskFile.setParent(parent);
                netDiskFile.setPossessor(parent.getPossessor());
                setParent(netDiskFile, parent);
            }else{
                netDiskFile.setPossessor(currentUser);
            }
            netDiskFile.setIsDirectory(false);
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
//        删除所有文件记录
        netDiskFileRepository.deleteAll(allFileAndDirectory);
//        删除上传文件实体
        for (var file : allFile) {
            try {
                var systemFile = systemFileFactory.fromNetDiskFile(file);
                if(!systemFile.delete()) throw new DeleteFileFailException(systemFile);
            }catch (Exception e){
                //出现任何异常都不进行回滚，因为即使回滚了也可能有一部分文件已经被删除了
                log.error("删除目录失败：path=" + file.getPath(), e);
            }
        }
//        删除所有目录, 从底部向上删除
        for (int i = allDirectory.size() - 1; i > -1; i--) {
            try {
                var file = systemFileFactory.fromNetDiskFile(allDirectory.get(i));
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
        var oldSystemFile = systemFileFactory.fromNetDiskFile(target);
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
        boolean canChangePermission = target.getPossessor().getId().equals(currentUser.getId());
        if(canChangePermission) {
            setNetDiskFilePermission(dto, target);
        }

//        持久化
        netDiskFileRepository.save(target);
//        执行实体文件操作
        if(!oldPath.equals(target.getPath())){
            if(!oldSystemFile.renameTo(target.getPath()))
                throw new RenameFileFailException(oldPath, target.getPath());
        }
//        vo
        var vo = new NetDiskFileVo();
        vo.setId(target.getId());
        vo.setName(target.getName());
        vo.setIsDirectory(target.getIsDirectory());
        if(canChangePermission){
            vo.setEveryoneReadable(target.getEveryoneReadable());
            vo.setEveryoneWritable(target.getEveryoneWritable());
            var allUsersId = new HashSet<>(dto.getReadableUserList());
            if(allUsersId.isEmpty()){
                vo.setReadableUserList(Collections.emptyList());
                vo.setWritableUserList(Collections.emptyList());
            }else {
                allUsersId.addAll(dto.getWritableUserList());
                var users = baseUserRepository.findAllById(allUsersId).stream().map(BaseUserVo::new).collect(Collectors.toList());
                vo.setReadableUserList(users.stream().filter(u -> dto.getReadableUserList().contains(u.getId())).collect(Collectors.toList()));
                vo.setWritableUserList(users.stream().filter(u -> dto.getWritableUserList().contains(u.getId())).collect(Collectors.toList()));
            }
        }
        return vo;
    }

    /**
     * 查询子文件夹文件数量
     */
    private List<NetDiskFileListVo> queryChildrenNum(List<NetDiskFileListVo> voList) {
        var childrenNumMap = netDiskFileRepository
                .findAllChildrenNum(
                        voList.stream()
                                .filter(NetDiskFileListVo::getIsDirectory)
                                .map(NetDiskFileListVo::getId)
                                .collect(Collectors.toList())
                )
                .stream()
                .collect(Collectors.toUnmodifiableMap(EntityLongValueVo::getId, EntityLongValueVo::getValue));
        voList.forEach(f->f.setChildrenNum(childrenNumMap.get(f.getId())));
        return voList;
    }

    @Override
    public List<NetDiskFileListVo> listChildrenVo(BaseUser user, Long id, Boolean isDirectory, String mediaType, String suffix, Sort sort) {
        Specification<NetDiskFile> specification;
        NetDiskFile target = null;
        if(user == null){
            if(id == null) throw new UnauthorizedException();
            specification = NetDiskFileSpecification.parentId(id);
            target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);
            checkReadPermission(null, target);
        }else{
            if(id == null){
                specification = NetDiskFileSpecification.selectRoot()
                        .and(NetDiskFileSpecification.possessorId(user.getId()));
            }else{
                specification = NetDiskFileSpecification.parentId(id);
                target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);
            }
        }
        if(target != null) checkReadPermission(user, target);
        if(isDirectory != null) specification = specification.and(NetDiskFileSpecification.isDirectory(isDirectory));
        if(mediaType != null) specification = specification.and(NetDiskFileSpecification.mediaTypeLike(mediaType));
        if(suffix != null) specification = specification.and(NetDiskFileSpecification.suffix(suffix));
        var fileList = netDiskFileRepository.findAll(specification, sort);
        return queryChildrenNum(fileList.stream().map(NetDiskFileListVo::new).collect(Collectors.toList()));
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
    public void checkWritePermission(BaseUser user, NetDiskFile target){
        if (!target.getEveryoneWritable()) {//若不是所有人都可写入
            if(user == null) throw new UnauthorizedException();
            if(!target.getPossessor().getId().equals(user.getId())) {//若不是所有者创建
                var writableUserIds = netDiskFileRepository.getWritableUserIds(target.getId());
                if(writableUserIds.stream().noneMatch(uid -> uid.equals(user.getId())))
                    throw new ForbiddenException();
            }
        }
    }

    @Override
    public ValueVo<String> getDownloadUrl(HttpServletRequest request, Long id, BaseUser user) {
        var target = netDiskFileRepository.findById(id).orElseThrow(NotFoundException::new);
        if(target.getIsDirectory()) throw new NotImplementedException();
        checkReadPermission(user, target);
        return new ValueVo<>(systemFileFactory.getDownloadUrl(request, target));
    }

    @Override
    public Optional<NetDiskFileVo> findVoById(Long id) {
        var userOptional = userAuditingListener.getCurrentAuditor();
        var result = netDiskFileRepository.findById(id).map(NetDiskFileVo::new);
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
    private void checkDeletePermission(@Nullable BaseUser user, NetDiskFile target) {
        checkWritePermission(user, target);
    }

    /**
     * 检测更新权限
     */
    private void checkUpdatePermission(@Nullable BaseUser user, NetDiskFile target) {
        checkWritePermission(user, target);
    }

    /**
     * 检测读取权限
     */
    @Override
    public void checkReadPermission(@Nullable BaseUser user, NetDiskFile target){
        if (!target.getEveryoneReadable()) {//若不是所有人都可读取
            if(user == null) throw new UnauthorizedException();
            if(!target.getPossessor().getId().equals(user.getId())) {//若不是所有者
                var readableUserList = netDiskFileRepository.getReadableUserIds(target.getId());
                if(readableUserList.stream().noneMatch(id -> id.equals(user.getId())))
                    throw new ForbiddenException();
            }
        }
    }
}
