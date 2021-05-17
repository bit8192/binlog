package cn.bincker.web.blog.netdisk.controller;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.service.IUploadService;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.service.dto.valid.CreateDirectoryValid;
import cn.bincker.web.blog.netdisk.service.dto.valid.UploadFileValid;
import cn.bincker.web.blog.netdisk.service.vo.NetDiskFileVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${system.base-path}/net-disk-files")
public class NetDiskFileController {
    private static Logger log = LoggerFactory.getLogger(NetDiskFileController.class);

    private final INetDiskFileService netDiskFileService;
    private final ISystemFileFactory systemFileFactory;
    private final String basePath;

    public NetDiskFileController(INetDiskFileService netDiskFileService, ISystemFileFactory systemFileFactory, @Value("${system.base-path}") String basePath) {
        this.netDiskFileService = netDiskFileService;
        this.systemFileFactory = systemFileFactory;
        this.basePath = basePath;
    }

    @GetMapping
    public CollectionModel<NetDiskFileVo> listChildren(@NonNull BaseUser baseUser, Long id) {
        var links = new Link[]{
                Link.of(basePath + "/net-disk-files" + (id == null ? "" : "?id=" + id)).withSelfRel(),
                Link.of(basePath + "/net-disk-files/{id}", "download"),
        };
        if(id == null) {
            return CollectionModel.of(netDiskFileService.listUserRootVo(baseUser.getId())).add(links);
        }else{
            return CollectionModel.of(netDiskFileService.listChildrenVo(id)).add(links);
        }
    }

    @PostMapping("directories")
    public EntityModel<NetDiskFileVo> createDirectory(@RequestBody @Validated(CreateDirectoryValid.class) NetDiskFileDto dto){
        return EntityModel.of(netDiskFileService.createDirectory(dto));
    }

    @PostMapping("files")
    public CollectionModel<NetDiskFileVo> upload(MultipartHttpServletRequest request, @RequestPart("fileInfo") @Validated(UploadFileValid.class) NetDiskFileDto dto){
        return CollectionModel.of(netDiskFileService.upload(
                request
                        .getFileMap()
                        .values()
                        .stream()
                        .filter(multipartFile -> !multipartFile.getName().equals("fileInfo"))
                        .collect(Collectors.toList()),
                dto
        ));
    }

    @GetMapping("{id}")
    public void download(@PathVariable Long id, Optional<BaseUser> userOptional, HttpServletResponse response){
        var netDiskFile = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
        if(netDiskFile.getIsDirectory()) throw new BadRequestException("暂不支持下载目录");
        if(!netDiskFile.getEveryoneReadable()){
            netDiskFileService.checkReadPermission(userOptional.orElseThrow(ForbiddenException::new), netDiskFile);
        }
        var file = systemFileFactory.fromNetDiskFile(netDiskFile);
        if(!file.exists()) throw new NotFoundException("文件不存在", "存在文件记录但文件不存在：netDiskFileId=" + netDiskFile.getId() + "\tpath=" + netDiskFile.getPath());
        response.setHeader("Content-Disposition", "attachment; filename=" + netDiskFile.getName());
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        try(var in = file.getInputStream(); var out = response.getOutputStream()){
            in.transferTo(out);
        } catch (IOException e) {
            log.error("下载文件失败: netDiskFileId=" + netDiskFile.getId() + "\tpath=" + netDiskFile.getPath());
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("{id}")
    public void del(@PathVariable Long id){
        netDiskFileService.delete(id);
    }
}
