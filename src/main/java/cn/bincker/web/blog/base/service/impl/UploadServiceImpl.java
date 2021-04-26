package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.base.service.IUploadService;
import cn.bincker.web.blog.base.service.dto.UploadFileDto;
import cn.bincker.web.blog.utils.CommonUtils;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UploadServiceImpl implements IUploadService {
    private final IUploadFileRepository repository;
    private final MultipartProperties multipartProperties;
    private final UserAuditingListener userAuditingListener;

    public UploadServiceImpl(IUploadFileRepository repository, MultipartProperties multipartProperties, UserAuditingListener userAuditingListener) {
        this.repository = repository;
        this.multipartProperties = multipartProperties;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public List<UploadFileDto> upload(Collection<MultipartFile> files, boolean isPublic) {
        File uploadDir;
        try {
            uploadDir = ResourceUtils.getFile(multipartProperties.getLocation());
        } catch (FileNotFoundException e) {
            uploadDir = new File(multipartProperties.getLocation());
            if(!uploadDir.mkdirs()){
                throw new SystemException("创建上传目录失败 path=" + uploadDir.getAbsolutePath());
            }
        }

        Optional<BaseUser> userOptional = userAuditingListener.getCurrentAuditor();
        if(userOptional.isPresent()) uploadDir = new File(uploadDir, userOptional.get().getUsername());
        uploadDir = new File(uploadDir, SimpleDateFormat.getDateInstance().format(new Date()));

        if(!uploadDir.exists() && !uploadDir.mkdirs()){
            throw new SystemException("创建上传目录失败 path=" + uploadDir.getAbsolutePath());
        }

        File finalUploadDir = uploadDir;
        return files.stream().map(multipartFile -> {
            UploadFile uploadFile = new UploadFile();
            uploadFile.setIsPublic(isPublic);
            uploadFile.setStorageLocation(UploadFile.StorageLocation.LOCAL);
            uploadFile.setSize(multipartFile.getSize());

            String fileName = multipartFile.getOriginalFilename();
            if(!StringUtils.hasText(fileName)) fileName = UUID.randomUUID().toString();
            File targetFile = new File(finalUploadDir, fileName);
            while (targetFile.exists()){
                fileName = UploadServiceImpl.nextSerialFileName(fileName);
                targetFile = new File(finalUploadDir, fileName);
            }
            uploadFile.setPath(targetFile.getPath());
            uploadFile.setName(fileName);
            uploadFile.setSuffix(CommonUtils.getStringSuffix(fileName, "."));
            try(
                    InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream());
                    OutputStream outputStream = new FileOutputStream(targetFile)
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

    private static final Pattern PATTERN_SERIAL_FILE_NAME = Pattern.compile("(\\S+)(-\\d+)?((\\.[\\w_]+)*)$");

    /**
     * 获取下一个序列文件名
     * 如：test-1.txt  test-2.txt  test-3.txt
     * @param filename 文件名
     */
    private static String nextSerialFileName(String filename){
        Matcher matcher = PATTERN_SERIAL_FILE_NAME.matcher(filename);
        if(matcher.find()) throw new SystemException("无效文件名 filename=" + filename);
        int index = 1;
        try{
            String indexStr = matcher.group(2);
            index = Integer.parseInt(indexStr.substring(1)) + 1;
        }catch (IllegalStateException ignore) {}
            return matcher.replaceFirst("$1-" + index + "$3");
    }
}
