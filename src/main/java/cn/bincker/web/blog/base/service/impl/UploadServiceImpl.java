package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.config.SystemFileProperties;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.ISystemFile;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.base.service.IUploadService;
import cn.bincker.web.blog.base.dto.UploadFileDto;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.DateUtils;
import cn.bincker.web.blog.utils.DigestUtils;
import cn.bincker.web.blog.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UploadServiceImpl implements IUploadService {
    private final IUploadFileRepository repository;
    private final UserAuditingListener userAuditingListener;
    private final DateUtils dateUtils;
    private final ISystemFileFactory systemFileFactory;
    private final SystemFileProperties systemFileProperties;

    public UploadServiceImpl(IUploadFileRepository repository, UserAuditingListener userAuditingListener, DateUtils dateUtils, ISystemFileFactory systemFileFactory, SystemFileProperties systemFileProperties) {
        this.repository = repository;
        this.userAuditingListener = userAuditingListener;
        this.dateUtils = dateUtils;
        this.systemFileFactory = systemFileFactory;
        this.systemFileProperties = systemFileProperties;
    }

    @Override
    public List<UploadFileDto> upload(Collection<MultipartFile> files, Boolean isPublic) {
        ISystemFile uploadDir;

        Optional<BaseUser> userOptional = userAuditingListener.getCurrentAuditor();
        uploadDir = userOptional
                .map(baseUser -> systemFileFactory.fromPath(systemFileProperties.getLocation(), baseUser.getUsername(), "public", dateUtils.today()))
                .orElseGet(() -> systemFileFactory.fromPath(systemFileProperties.getLocation(), "public", dateUtils.today()));

        if(!uploadDir.exists() && !uploadDir.mkdirs()) throw new SystemException("创建目录失败: path=" + uploadDir.getPath());
        String finalUploadDir = uploadDir.getPath();
        return files.stream().map(multipartFile -> {
            UploadFile uploadFile = new UploadFile();
            uploadFile.setIsPublic(userOptional.isPresent() ? isPublic : true);//权限只有登录的用户可以设置, 否则都是公开
            uploadFile.setSize(multipartFile.getSize());

            String fileName = multipartFile.getOriginalFilename();
            if(!StringUtils.hasText(fileName)) fileName = UUID.randomUUID().toString();
            fileName = fileName.replaceAll(RegexpConstant.ILLEGAL_FILE_NAME_CHAR_VALUE, "");
            var targetFile = systemFileFactory.fromPath(finalUploadDir, fileName);
            while (targetFile.exists()){
                fileName = FileUtils.nextSerialFileName(fileName);
                targetFile = systemFileFactory.fromPath(finalUploadDir, fileName);
            }
            uploadFile.setPath(targetFile.getPath());
            uploadFile.setName(fileName);
            uploadFile.setSuffix(CommonUtils.getStringSuffix(fileName, "."));
            uploadFile.setMediaType(multipartFile.getContentType());
            try(var in = multipartFile.getInputStream()){
                uploadFile.setSha256(DigestUtils.sha256Hex(in));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new SystemException(e);
            }
            try(var in = multipartFile.getInputStream(); var out = targetFile.getOutputStream()){
                in.transferTo(out);
            } catch (IOException e) {
                throw new SystemException(e);
            }
            repository.save(uploadFile);

            return UploadServiceImpl.getDto(uploadFile);
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<UploadFile> getUploadFile(Long id) {
        return repository.findById(id);
    }

    private static UploadFileDto getDto(UploadFile uploadFile){
        UploadFileDto dto = new UploadFileDto();
        dto.setId(uploadFile.getId());
        dto.setName(uploadFile.getName());
        dto.setSize(uploadFile.getSize());
        dto.setMediaType(uploadFile.getMediaType());
        dto.setSuffix(uploadFile.getSuffix());
        dto.setUrl("/api/files/" + uploadFile.getId());
        return dto;
    }
}
