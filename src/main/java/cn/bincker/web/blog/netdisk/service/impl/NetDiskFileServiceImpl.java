package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.base.config.SystemFileProperties;
import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.*;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.vo.EntityLongValueVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.exception.DeleteFileFailException;
import cn.bincker.web.blog.netdisk.exception.MakeDirectoryFailException;
import cn.bincker.web.blog.netdisk.exception.RenameFileFailException;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.base.entity.ISystemFile;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.specification.NetDiskFileSpecification;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.DigestUtils;
import cn.bincker.web.blog.utils.FileUtils;
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
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NetDiskFileServiceImpl implements INetDiskFileService {
    private static final Logger log = LoggerFactory.getLogger(NetDiskFileServiceImpl.class);

    private final INetDiskFileRepository netDiskFileRepository;
    private final UserAuditingListener userAuditingListener;
    private final ISystemFileFactory systemFileFactory;
    private final IBaseUserRepository baseUserRepository;
    private final SystemFileProperties systemFileProperties;

    public NetDiskFileServiceImpl(
            INetDiskFileRepository netDiskFileRepository,
            UserAuditingListener userAuditingListener,
            ISystemFileFactory systemFileFactory,
            IBaseUserRepository baseUserRepository, SystemFileProperties systemFileProperties) {
        this.netDiskFileRepository = netDiskFileRepository;
        this.userAuditingListener = userAuditingListener;
        this.systemFileFactory = systemFileFactory;
        this.baseUserRepository = baseUserRepository;
        this.systemFileProperties = systemFileProperties;
    }

    private NetDiskFile commonCreateDirectory(NetDiskFileDto dto){
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);

        if(dto.getName().matches(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE))
            throw new BadRequestException("无效文件名");

//        构建实体
        var target = new NetDiskFile();
        target.setName(dto.getName());
        target.setIsDirectory(true);
        target.setFileSystemTypeSet(Collections.singleton(dto.getFileSystemType()));

//        如果有父级，那么设置所有者，并设置路径
        ISystemFile targetPath;
        if(dto.getParentId() != null){
            var parent = netDiskFileRepository.findById(dto.getParentId())
                    .orElseThrow(()->new NotFoundException("父级目录不存在", "创建目录时父级目录不存在： NetDiskFile.id=" + dto.getParentId()));
            //是否有相同的目录
            var sameDir = netDiskFileRepository.findByPath(FileUtils.join(parent.getPath(), dto.getName()));
            if(sameDir.isPresent()){
                target = sameDir.get();
                if(target.getFileSystemTypeSet().contains(dto.getFileSystemType())) throw new SystemException("文件夹已存在");
                target.getFileSystemTypeSet().add(dto.getFileSystemType());
                targetPath = systemFileFactory.fromNetDiskFile(target);
            } else {
                //判断是否有权限创建
                checkWritePermission(currentUser, parent);
                target.setPossessor(parent.getPossessor());
                targetPath = systemFileFactory.fromPath(dto.getFileSystemType(), parent.getPath(), dto.getName() + "/");
                target.setPath(targetPath.getPath());
                target.setParent(parent);
                setParents(target, parent);
            }
        }
//        否则路径是用户根路径，父级为空，所有者为自己
        else{
            //判断是否有相同的目录
            var sameDir = netDiskFileRepository.findByPath(FileUtils.join(currentUser.getUsername(), dto.getName()));
            if(sameDir.isPresent()){
                target = sameDir.get();
                if(target.getFileSystemTypeSet().contains(dto.getFileSystemType())) throw new SystemException("文件夹已存在");
                target.getFileSystemTypeSet().add(dto.getFileSystemType());
                targetPath = systemFileFactory.fromNetDiskFile(target);
            }else{
                targetPath = systemFileFactory.fromPath(dto.getFileSystemType(), currentUser.getUsername(), dto.getName() + "/");
                target.setPath(targetPath.getPath());
                target.setPossessor(currentUser);
            }
        }

//            所有者且没有重复目录才可以修改权限
        if(target.getId() == null && target.getPossessor().getId().equals(currentUser.getId())){
            setNetDiskFilePermission(dto, target);
        }

//        持久化
        netDiskFileRepository.save(target);

//        创建目录放在最后面执行
        if(!targetPath.exists() && !targetPath.mkdirs()) throw new MakeDirectoryFailException(targetPath);

        return target;
    }

    @Override
    @Transactional
    public NetDiskFileVo createDirectory(NetDiskFileDto dto) {
        return new NetDiskFileVo(commonCreateDirectory(dto));
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
        Optional<NetDiskFile> parentOptional = Optional.empty();
        String targetPath;
        if(dto.getParentId() != null){
            parentOptional = netDiskFileRepository.findById(dto.getParentId());
            if(parentOptional.isEmpty()) throw new NotFoundException();
            //如果存储类型必须是父目录中的一种
            if(!parentOptional.get().getFileSystemTypeSet().contains(dto.getFileSystemType())) throw new SystemException("请先创建对应存储类型的父级目录");
            targetPath = parentOptional.get().getPath();
        }else{
            var targetPathSystemFile = systemFileFactory.fromPath(dto.getFileSystemType(), currentUser.getUsername());
            if(!targetPathSystemFile.exists() && !targetPathSystemFile.mkdirs()) throw new SystemException("创建文件夹失败");
            targetPath = targetPathSystemFile.getPath();
        }
        var result = new ArrayList<NetDiskFileVo>(multipartFiles.size());
        for (MultipartFile multipartFile : multipartFiles) {
            //构建uploadFile
            NetDiskFile netDiskFile = buildNetDiskFile(dto, currentUser, parentOptional, targetPath, multipartFile);
            //持久化
            netDiskFileRepository.save(netDiskFile);
            //填上用户数据, 因为这里的用户只有id, 查也查不出来（被缓存了吧，能想到的办法只有手动查）
            netDiskFile.setReadableUserList(new HashSet<>(baseUserRepository.findAllById(dto.getReadableUserList())));
            netDiskFile.setWritableUserList(new HashSet<>(baseUserRepository.findAllById(dto.getWritableUserList())));
            result.add(new NetDiskFileVo(netDiskFile));
        }
        return result;
    }

    private NetDiskFile buildNetDiskFile(NetDiskFileDto dto, BaseUser currentUser, Optional<NetDiskFile> parentOptional, String targetPath, MultipartFile multipartFile) {
        var netDiskFile = new NetDiskFile();
        netDiskFile.setFileSystemTypeSet(Collections.singleton(dto.getFileSystemType()));
        if(StringUtils.hasText(dto.getName())){
            netDiskFile.setName(dto.getName().replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE, ""));
        }else if(StringUtils.hasText(multipartFile.getOriginalFilename())){
            netDiskFile.setName(multipartFile.getOriginalFilename().replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE, ""));
        }else{
            netDiskFile.setName(UUID.randomUUID().toString());
        }
        while (systemFileFactory.fromPath(dto.getFileSystemType(), targetPath, netDiskFile.getName()).exists())
            netDiskFile.setName(FileUtils.nextSerialFileName(netDiskFile.getName()));
        netDiskFile.setPath(FileUtils.join(targetPath, netDiskFile.getName()));
        netDiskFile.setSuffix(CommonUtils.getStringSuffix(netDiskFile.getName(), "."));//后缀全用小写，方便查询
        netDiskFile.setMediaType(multipartFile.getContentType());
        netDiskFile.setSize(multipartFile.getSize());
        //写到文件, 并计算sha256
        var systemFile = systemFileFactory.fromPath(dto.getFileSystemType(), targetPath, netDiskFile.getName());
        while (systemFile.exists()){
            systemFile = systemFileFactory.fromPath(dto.getFileSystemType(), targetPath, FileUtils.nextSerialFileName(systemFile.getName()));
        }
        try(var in = multipartFile.getInputStream()){
            netDiskFile.setSha256(DigestUtils.sha256Hex(in));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new SystemException(e);
        }
        try(var in = multipartFile.getInputStream(); var out = systemFile.getOutputStream()){
            in.transferTo(out);
        } catch (IOException e) {
            throw new SystemException(e);
        }
        if(!systemFile.exists()){
            throw new SystemException("上传文件失败:path=" + systemFile.getPath());
        }
        if(parentOptional.isPresent()){
            var parent = parentOptional.get();
            netDiskFile.setParent(parent);
            netDiskFile.setPossessor(parent.getPossessor());
            setParents(netDiskFile, parent);
        }else{
            netDiskFile.setPossessor(currentUser);
        }
        netDiskFile.setIsDirectory(false);
        //如果是持有者，那么设置权限
        if(currentUser.getId().equals(netDiskFile.getPossessor().getId())){
            setNetDiskFilePermission(dto, netDiskFile);
        }
        return netDiskFile;
    }

    /**
     * 设置父级
     */
    private void setParents(NetDiskFile netDiskFile, NetDiskFile parent) {
        if(parent == null) {
            netDiskFile.setParents(Collections.emptyList());
            return;
        }
        var parents = parent.getParents() == null ? new ArrayList<Long>() : new ArrayList<>(parent.getParents());
        parents.add(parent.getId());
        netDiskFile.setParents(parents);
    }

    /**
     * 删除目录或文件，这个操作是无法回滚的，因为即便数据被回滚了，文件也被删了
     */
    @Override
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
//        先删除文件
        for (var file : allFile) {
            try {
                var systemFile = systemFileFactory.fromNetDiskFile(file);
                if(systemFile.exists() && !systemFile.delete()) throw new DeleteFileFailException(systemFile);
                netDiskFileRepository.delete(file);
            }catch (Exception e){
                log.error("删除文件失败：path=" + file.getPath(), e);
                throw new DeleteFileFailException(file.getPath(), e);
            }
        }
//        删除所有目录, 从底部向上删除
        for (int i = allDirectory.size() - 1; i > -1; i--) {
            var directory = allDirectory.get(i);
            try {
                var file = systemFileFactory.fromNetDiskFile(directory);
                if(file.exists() && !file.delete()) throw new DeleteFileFailException(file);
                netDiskFileRepository.delete(directory);
            }catch (Exception e){
                log.error("删除目录失败：path=" + directory, e);
                throw new DeleteFileFailException(directory.getPath(), e);
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
//        是否需要修改子节点路径
        var needUpdateChildrenPath = false;
//        更新对象
        var target = netDiskFileRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
//        排序的所有子孙节点, 层级高的靠前，低层的靠后，这样修改路径才不会出错
        var allChildren = netDiskFileRepository.findAll(NetDiskFileSpecification.containsParentId(target.getId()))
                .stream().sorted(Comparator.comparingInt(a -> a.getParents().size())).collect(Collectors.toList());
//        父节点Map, 方便查找父节点
        var parentNetDiskFileMap = new HashMap<>(allChildren.stream().collect(Collectors.toUnmodifiableMap(NetDiskFile::getId, f -> f)));
        parentNetDiskFileMap.put(target.getId(), target);
//        原来的文件
        var oldSystemFile = systemFileFactory.fromNetDiskFile(target);
//        权限判断
        checkUpdatePermission(currentUser, target);
//        移动操作
        var oldPath = target.getPath();
        if(dto.getParentId() != null || target.getParent() != null){//如果传入parent不为空，或者传入parent未空但原有parent不为空
            if(dto.getParentId() == null){//传入parent为空则移动到根目录
                target.setParent(null);
                target.setPath(FileUtils.join(currentUser.getUsername(), dto.getName()) + (target.getIsDirectory() ? "/" : ""));
                setParents(target, null);
            }else{
                var parent = netDiskFileRepository.findById(dto.getParentId())
                        .orElseThrow(()->new NotFoundException("父级不存在", "移动操作失败父级节点不存在：id=" + dto.getParentId()));
                target.setParent(parent);
                target.setPath(new File(new File(parent.getPath()), dto.getName()).getPath() + (target.getIsDirectory() ? "/" : ""));
                setParents(target, parent);
            }
//            修改所有子节点的父节点列表
            for (NetDiskFile netDiskFile : allChildren) {
                var parent = parentNetDiskFileMap.get(netDiskFile.getParent().getId());
                setParents(netDiskFile, parent);
            }
            needUpdateChildrenPath = target.getIsDirectory();
        }
//        重命名
        if(!dto.getName().equals(target.getName())){
            target.setName(dto.getName());
            target.setPath(new File(new File(target.getPath()).getParentFile(), target.getName()).getPath() + (target.getIsDirectory() ? "/" : ""));
            needUpdateChildrenPath = target.getIsDirectory();
        }
//        修改子节点路径
        if(needUpdateChildrenPath){
//            修改所有子节点路径
            for (NetDiskFile netDiskFile : allChildren) {
                var parent = parentNetDiskFileMap.get(netDiskFile.getParent().getId());
                netDiskFile.setPath(new File(new File(parent.getPath()), netDiskFile.getName()).getPath() + (netDiskFile.getIsDirectory() ? "/" : ""));
            }
        }
//        只有所有者才能修改权限
        boolean canChangePermission = target.getPossessor().getId().equals(currentUser.getId());
        if(canChangePermission) {
            setNetDiskFilePermission(dto, target);
        }

//        持久化
        netDiskFileRepository.save(target);
        netDiskFileRepository.saveAll(allChildren);
//        执行实体文件操作
        if(!oldPath.equals(target.getPath())){
            if(!oldSystemFile.renameTo(target.getPath()))
                throw new RenameFileFailException(oldPath, target.getPath());
        }
//        vo
        var vo = new NetDiskFileVo(target);
        if(!canChangePermission){
            vo.setEveryoneReadable(null);
            vo.setEveryoneWritable(null);
            vo.setReadableUserList(Collections.emptyList());
            vo.setWritableUserList(Collections.emptyList());
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
        if(StringUtils.hasText(mediaType)) specification = specification.and(NetDiskFileSpecification.mediaTypeLike(mediaType));
        if(StringUtils.hasText(suffix)) specification = specification.and(NetDiskFileSpecification.suffix(suffix));
        var fileList = netDiskFileRepository.findAll(specification, sort);
        return queryChildrenNum(fileList.stream().map(NetDiskFileListVo::new).collect(Collectors.toList()));
    }

    @Override
    public List<NetDiskFile> findAllById(List<Long> ids) {
        return netDiskFileRepository.findAllById(ids);
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

    @Override
    @Transactional
    public NetDiskFileVo uploadMaterial(String group, MultipartFile multipartFile) {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow(ForbiddenException::new);
        var storeRootPath = FileUtils.join(user.getUsername(), systemFileProperties.getMaterialStoreLocation()) + File.separator;
        //获取/创建存储根目录
        var storeRoot = netDiskFileRepository.findByPath(storeRootPath).orElseGet(() -> {
            var dto = new NetDiskFileDto();
            dto.setName(systemFileProperties.getMaterialStoreLocation());
            dto.setEveryoneReadable(false);
            dto.setEveryoneWritable(false);
            dto.setReadableUserList(Collections.emptySet());
            dto.setWritableUserList(Collections.emptySet());
            dto.setFileSystemType(systemFileProperties.getMaterialStoreType());
            return commonCreateDirectory(dto);
        });
        //获取/创建存储分组目录
        var storePath = netDiskFileRepository.findByPath(FileUtils.join(storeRoot.getPath(), group) + File.separator).orElseGet(() -> {
            var dto = new NetDiskFileDto();
            dto.setName(group);
            dto.setParentId(storeRoot.getId());
            dto.setEveryoneReadable(false);
            dto.setEveryoneWritable(false);
            dto.setReadableUserList(Collections.emptySet());
            dto.setWritableUserList(Collections.emptySet());
            dto.setFileSystemType(systemFileProperties.getMaterialStoreType());
            return commonCreateDirectory(dto);
        });

        var targetDto = new NetDiskFileDto();
        targetDto.setParentId(storePath.getId());
        targetDto.setFileSystemType(systemFileProperties.getMaterialStoreType());
        targetDto.setWritableUserList(Collections.emptySet());
        targetDto.setReadableUserList(Collections.emptySet());
        targetDto.setEveryoneWritable(false);
        targetDto.setEveryoneReadable(true);
        var target = buildNetDiskFile(targetDto, user, Optional.of(storePath), storePath.getPath(), multipartFile);
        return new NetDiskFileVo(netDiskFileRepository.save(target));
    }
}
