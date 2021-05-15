package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.service.IUploadService;
import cn.bincker.web.blog.base.service.dto.UploadFileDto;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Optional;

@RestController
@RequestMapping("${system.base-path}/files")
public class UploadController {
    private final IUploadService uploadService;

    public UploadController(IUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public CollectionModel<UploadFileDto> upload(MultipartHttpServletRequest request, @RequestParam(name = "isPublic", defaultValue = "true") boolean isPublic){
        return CollectionModel.of(uploadService.upload(request.getFileMap().values(), isPublic));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, BaseUser user, HttpServletResponse response){
        Optional<UploadFile> uploadFileOptional = uploadService.getUploadFile(id);
        if(uploadFileOptional.isEmpty()) return ResponseEntity.notFound().build();
        UploadFile uploadFile = uploadFileOptional.get();
        //判断是否有权限读取
        if(!uploadFile.getIsPublic()){
            BaseUser createUser = uploadFile.getCreatedUser();
            if(createUser == null && user.getRoles().stream().noneMatch(role -> role.getCode().equals(Role.RoleEnum.ADMIN.toString()))){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        File targetFile = new File(uploadFile.getPath());
        response.setHeader("Content-Disposition", "attachment; filename=" + uploadFile.getName());
        return ResponseEntity.ok(new FileSystemResource(targetFile));
    }
}
