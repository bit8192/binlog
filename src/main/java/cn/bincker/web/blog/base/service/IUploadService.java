package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.dto.UploadFileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IUploadService {
    List<UploadFileDto> upload(Collection<MultipartFile> files, Boolean isPublic);

    Optional<UploadFile> getUploadFile(Long id);
}
