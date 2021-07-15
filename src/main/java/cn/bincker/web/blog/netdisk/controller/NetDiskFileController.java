package cn.bincker.web.blog.netdisk.controller;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.dto.valid.CreateDirectoryValid;
import cn.bincker.web.blog.netdisk.dto.valid.UploadFileValid;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${system.base-path}/net-disk-files")
public class NetDiskFileController {
    private static final Logger log = LoggerFactory.getLogger(NetDiskFileController.class);

    private final INetDiskFileService netDiskFileService;
    private final ISystemFileFactory systemFileFactory;

    public NetDiskFileController(INetDiskFileService netDiskFileService, ISystemFileFactory systemFileFactory) {
        this.netDiskFileService = netDiskFileService;
        this.systemFileFactory = systemFileFactory;
    }

    @GetMapping(value = "{id}")
    public NetDiskFileVo getItem(@PathVariable Long id){
        return netDiskFileService.findVoById(id).orElseThrow(NotFoundException::new);
    }

    @GetMapping
    public Collection<NetDiskFileListVo> listChildren(
            BaseUser user,
            Long id,
            Boolean isDirectory,
            String mediaType,
            String suffix,
            Sort sort
    ) {
        if(sort.isUnsorted())
            sort = Sort.by(Sort.Order.desc("isDirectory"), Sort.Order.by("createdDate"));
        return netDiskFileService.listChildrenVo(user, id, isDirectory, mediaType, suffix, sort);
    }

    @GetMapping("{id}/parents")
    public Collection<NetDiskFileListVo> getParents(BaseUser user, @PathVariable Long id){
        var target = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
        netDiskFileService.checkReadPermission(user, target);
        var parents = netDiskFileService.findAllById(target.getParents());
        parents.sort((a,b)->b.getParents().length - a.getParents().length);
        var result = new ArrayList<NetDiskFileListVo>(parents.size());
        for (var currentItem : parents) {
            netDiskFileService.checkWritePermission(user, currentItem);
            result.add(parents.stream().filter(item -> item.getId().equals(currentItem.getId())).findFirst().map(NetDiskFileListVo::new).orElseThrow());
        }
        Collections.reverse(result);
        return result;
    }

    @PostMapping("directories")
    public NetDiskFileVo createDirectory(@RequestBody @Validated(CreateDirectoryValid.class) NetDiskFileDto dto){
        return netDiskFileService.createDirectory(dto);
    }

    @PostMapping("files")
    public Collection<NetDiskFileVo> upload(MultipartHttpServletRequest request, @RequestPart("fileInfo") @Validated(UploadFileValid.class) NetDiskFileDto dto){
        return netDiskFileService.upload(
                request
                        .getFileMap()
                        .values()
                        .stream()
                        .filter(multipartFile -> !multipartFile.getName().equals("fileInfo"))
                        .collect(Collectors.toList()),
                dto
        );
    }

    /**
     * 更改文件, 包括移动、重命名、修改权限等
     */
    @PutMapping
    public NetDiskFileVo put(@RequestBody NetDiskFileDto dto){
        return netDiskFileService.save(dto);
    }

    @GetMapping(value = "download/{id}")
    public void download(@PathVariable Long id, BaseUser user, HttpServletResponse response){
        outputFile(id, user, response, true);
    }

    @GetMapping(value = "get/{id}")
    public void get(@PathVariable Long id, BaseUser user, HttpServletResponse response){
        outputFile(id, user, response, false);
    }

    /**
     * 输出文件
     * @param id 文件id
     * @param user 用户
     * @param response response
     * @param isDownload 是否是下载文件
     */
    private void outputFile(Long id, BaseUser user, HttpServletResponse response, boolean isDownload){
        var netDiskFile = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
        if(netDiskFile.getIsDirectory()) throw new BadRequestException("暂不支持下载目录");
        if(!netDiskFile.getEveryoneReadable()){
            netDiskFileService.checkReadPermission(user, netDiskFile);
        }
        var file = systemFileFactory.fromNetDiskFile(netDiskFile);
        if(!file.exists()) throw new NotFoundException("文件不存在", "存在文件记录但文件不存在：netDiskFileId=" + netDiskFile.getId() + "\tpath=" + netDiskFile.getPath());
        if(isDownload) response.setHeader("Content-Disposition", "attachment; filename=" + netDiskFile.getName());
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
