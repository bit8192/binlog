package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.service.IUploadService;
import cn.bincker.web.blog.base.dto.UploadFileDto;
import cn.bincker.web.blog.utils.ResponseUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/files")
public class UploadController {
    private final IUploadService uploadService;

    public UploadController(IUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public Collection<UploadFileDto> upload(MultipartHttpServletRequest request, @RequestParam(required = false, defaultValue = "true") Boolean isPublic){
        return uploadService.upload(request.getFileMap().values(), isPublic);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, BaseUser user, HttpServletRequest request, HttpServletResponse response){
        String referer = request.getHeader(HttpHeaders.REFERER);
        if(StringUtils.hasText(referer) && !referer.equals("http://localhost:8080/")){
            var matcher = RegexpConstant.URL_HOST.matcher(referer);
            if(!matcher.find()) throw new BadRequestException();
            var refererHost = matcher.group(1);
            var host = request.getServerName();
            var serverPort = request.getServerPort();
            if(serverPort != 80 && serverPort != 443) host += ":" + serverPort;
            if(!host.equals(refererHost)) throw new ForbiddenException();
        }

        Optional<UploadFile> uploadFileOptional = uploadService.getUploadFile(id);
        if(uploadFileOptional.isEmpty()) return ResponseEntity.notFound().build();
        UploadFile uploadFile = uploadFileOptional.get();
//        检测缓存
        if(ResponseUtils.checkETag(request, response, uploadFile.getSha256())) return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        if(ResponseUtils.checkLastModified(request, response, uploadFile.getLastModifiedDate())) return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        //判断是否有权限读取
        if(!uploadFile.getIsPublic()){
            BaseUser createUser = uploadFile.getCreatedUser();
            if(createUser == null){
                if(user.getRoles().stream().noneMatch(role -> role.getCode().equals(Role.RoleEnum.ADMIN.toString()))) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }else if (!user.getId().equals(createUser.getId())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        File targetFile = new File(uploadFile.getPath());
//        response.setHeader("Content-Disposition", "attachment; filename=" + uploadFile.getName());
        ResponseUtils.setCachePeriod(response, Duration.ofDays(30));
        return ResponseEntity.ok(new FileSystemResource(targetFile));
    }
}
