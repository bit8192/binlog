package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.base.service.IUploadService;
import cn.bincker.web.blog.base.dto.UploadFileDto;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.DateUtils;
import cn.bincker.web.blog.utils.FileUtils;
import cn.bincker.web.blog.utils.SystemResourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UploadServiceImpl implements IUploadService {
    private final IUploadFileRepository repository;
    private final UserAuditingListener userAuditingListener;
    private final DateUtils dateUtils;
    private final SystemResourceUtils systemResourceUtils;
    private final ISystemFileFactory systemFileFactory;

    public UploadServiceImpl(IUploadFileRepository repository, UserAuditingListener userAuditingListener, DateUtils dateUtils, SystemResourceUtils systemResourceUtils, ISystemFileFactory systemFileFactory) {
        this.repository = repository;
        this.userAuditingListener = userAuditingListener;
        this.dateUtils = dateUtils;
        this.systemResourceUtils = systemResourceUtils;
        this.systemFileFactory = systemFileFactory;
    }

    @Override
    public List<UploadFileDto> upload(Collection<MultipartFile> files, Boolean isPublic) {
        String uploadDir;

        Optional<BaseUser> userOptional = userAuditingListener.getCurrentAuditor();
        uploadDir = userOptional
                .map(baseUser -> systemResourceUtils.getUploadPath(baseUser.getUsername() + File.separator + "public" + File.separator + dateUtils.today()).getPath())
                .orElseGet(() -> systemResourceUtils.getUploadPath("public" + File.separator + dateUtils.today()).getPath());

        String finalUploadDir = uploadDir;
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
            try(
                    InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream());
                    OutputStream outputStream = targetFile.getOutputStream()
            ){
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] buf = new byte[8192];
                int len;
                while ((len = inputStream.read(buf)) > 0){
                    messageDigest.update(buf, 0, len);
                    outputStream.write(buf, 0, len);
                }
                uploadFile.setSha256(CommonUtils.bytes2hex(messageDigest.digest()));
                repository.save(uploadFile);
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new SystemException(e);
            }

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
