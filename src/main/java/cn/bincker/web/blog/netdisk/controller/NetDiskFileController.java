package cn.bincker.web.blog.netdisk.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.enumeration.FileSystemTypeEnum;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.base.config.SystemFileProperties;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.dto.valid.CreateDirectoryValid;
import cn.bincker.web.blog.netdisk.dto.valid.UploadFileValid;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static cn.bincker.web.blog.base.service.impl.SystemFileFactoryImpl.CACHE_KEY_DOWNLOAD_CODE;

@RestController
@RequestMapping("/net-disk-files")
@ApiController
public class NetDiskFileController {
    private static final Logger log = LoggerFactory.getLogger(NetDiskFileController.class);

    private final INetDiskFileService netDiskFileService;
    private final ISystemFileFactory systemFileFactory;
    private final ISystemCacheService systemCacheService;
    private final SystemFileProperties systemFileProperties;

    public NetDiskFileController(INetDiskFileService netDiskFileService, ISystemFileFactory systemFileFactory, ISystemCacheService systemCacheService, SystemFileProperties systemFileProperties) {
        this.netDiskFileService = netDiskFileService;
        this.systemFileFactory = systemFileFactory;
        this.systemCacheService = systemCacheService;
        this.systemFileProperties = systemFileProperties;
    }

    /**
     * 获取可用的文件存储位置
     */
    @GetMapping("file-system-type/available")
    public List<FileSystemTypeEnum> getAvailableFileSystemType(){
        var result = new ArrayList<FileSystemTypeEnum>();
        result.add(FileSystemTypeEnum.LOCAL);
        if(systemFileProperties.getAliyunOss() != null){
            result.add(FileSystemTypeEnum.ALI_OSS);
        }
        return result;
    }

    /**
     * 通过ID获取文件信息
     */
    @GetMapping(value = "{id}")
    public NetDiskFileVo getItem(@PathVariable Long id){
        return netDiskFileService.findVoById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * 列出文件列表
     */
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

    /**
     * 通过id获取当前文件的父级目录列表
     */
    @GetMapping("{id}/parents")
    public Collection<NetDiskFileListVo> getParents(BaseUser user, @PathVariable Long id){
        var target = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
        netDiskFileService.checkReadPermission(user, target);
        var parents = netDiskFileService.findAllById(target.getParents());
        parents.sort((a,b)->b.getParents().size() - a.getParents().size());
        var result = new ArrayList<NetDiskFileListVo>(parents.size());
        for (var currentItem : parents) {
            netDiskFileService.checkWritePermission(user, currentItem);
            result.add(parents.stream().filter(item -> item.getId().equals(currentItem.getId())).findFirst().map(NetDiskFileListVo::new).orElseThrow());
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * 创建文件夹
     */
    @PostMapping("directories")
    public NetDiskFileVo createDirectory(@RequestBody @Validated(CreateDirectoryValid.class) NetDiskFileDto dto){
        return netDiskFileService.createDirectory(dto);
    }

    /**
     * 上传文件
     */
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
     * 上传素材
     * @param group 分组，通常为文章标题
     * @param material 素材文件
     */
    @PostMapping("materials")
    public NetDiskFileVo uploadMaterials(@RequestPart("group") String group, @RequestPart("material") MultipartFile material){
        return netDiskFileService.uploadMaterial(group, material);
    }

    /**
     * 更改文件, 包括移动、重命名、修改权限等
     */
    @PutMapping
    public NetDiskFileVo put(@RequestBody NetDiskFileDto dto){
        return netDiskFileService.save(dto);
    }

    /**
     * 获取下载链接
     */
    @GetMapping(value = "download-url/{id}")
    public ValueVo<String> getDownloadUrl(HttpServletRequest request, @PathVariable Long id, BaseUser user){
        return netDiskFileService.getDownloadUrl(request, id, user);
    }

    /**
     * 通过生成的下载链接下载，生成的下载链接是一次性的，不会做防盗链判断
     */
    @GetMapping(value = "download/{id}")
    public void download(@PathVariable Long id, BaseUser user, HttpServletResponse response, @RequestParam String code){
        var netDiskFile = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
        if(!systemCacheService.containsKey(CACHE_KEY_DOWNLOAD_CODE + code)) throw new ForbiddenException();
        systemCacheService.remove(CACHE_KEY_DOWNLOAD_CODE + code);
        outputFile(netDiskFile, user, response, true);
    }

    /**
     * 直接获取资源, 会做防盗链判断
     */
    @GetMapping(value = "get/{id}")
    public void get(@PathVariable Long id, BaseUser user, HttpServletRequest request, HttpServletResponse response){
        var netDiskFile = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
        if(ResponseUtils.checkETag(request, response, netDiskFile.getSha256())) return;
        if(ResponseUtils.checkLastModified(request, response, netDiskFile.getLastModifiedDate())) return;
        String referer = request.getHeader(HttpHeaders.REFERER);
        if(StringUtils.hasText(referer)){
            var matcher = RegexpConstant.URL_HOST.matcher(referer);
            if(!matcher.find()) throw new BadRequestException();
            var host = matcher.group(1);
            if(Arrays.stream(systemFileProperties.getAllowReferer()).noneMatch(p-> CommonUtils.simpleMatch(p, host))){
                throw new ForbiddenException();
            }
        }else if(!systemFileProperties.getAllowEmptyReferer()) throw new ForbiddenException();
        ResponseUtils.setCachePeriod(response, Duration.ofDays(30));
        outputFile(netDiskFile, user, response, false);
    }

    /**
     * 输出文件
     * @param netDiskFile 输出文件
     * @param user 用户
     * @param response response
     * @param isDownload 是否是下载文件
     */
    private void outputFile(NetDiskFile netDiskFile, BaseUser user, HttpServletResponse response, boolean isDownload){
        if(netDiskFile.getIsDirectory()) throw new BadRequestException("暂不支持下载目录");
        if(!netDiskFile.getEveryoneReadable()){
            netDiskFileService.checkReadPermission(user, netDiskFile);
        }
        var file = systemFileFactory.fromNetDiskFile(netDiskFile);
        if(!file.exists()) throw new NotFoundException("文件不存在", "存在文件记录但文件不存在：netDiskFileId=" + netDiskFile.getId() + "\tpath=" + netDiskFile.getPath());
        if(isDownload) response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(netDiskFile.getName(), StandardCharsets.UTF_8));
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        try(var in = file.getInputStream(); var out = response.getOutputStream()){
            in.transferTo(out);
        } catch (IOException e) {
            log.error("下载文件失败: netDiskFileId=" + netDiskFile.getId() + "\tpath=" + netDiskFile.getPath());
            throw new SystemException(e);
        }
    }

    /**
     * 缩略图
     */
    @GetMapping("thumbnail/{id}/{size}")
    public void thumbnail(@PathVariable Long id, BaseUser user, @PathVariable Integer size, HttpServletRequest request, HttpServletResponse response){
        if(size == null || size < 40 || size > 200) throw new BadRequestException();
        var netDiskFile = netDiskFileService.findById(id).orElseThrow(NotFoundException::new);
//        浏览器缓存检测
        if(ResponseUtils.checkETag(request, response, netDiskFile.getSha256())) return;
        if(ResponseUtils.checkLastModified(request, response, netDiskFile.getLastModifiedDate())) return;
//        是否是图片判断
        if(netDiskFile.getIsDirectory() || !netDiskFile.getMediaType().contains("image")) throw new NotFoundException();
//        权限检测
        if(!netDiskFile.getEveryoneReadable()) netDiskFileService.checkReadPermission(user, netDiskFile);
//        服务器缓存
        ResponseUtils.setCachePeriod(response, Duration.ofDays(30));
        var file = new File(systemFileProperties.getImageCacheLocation(), netDiskFile.getSha256() + "_" + size + ".webp");
        if(file.exists()) {
            try(var in = new FileInputStream(file)) {
                in.transferTo(response.getOutputStream());
            } catch (IOException e) {
                log.error("写出图片异常", e);
            }
            return;
        }
//        生成图片缓存
        BufferedImage image;
        try {
            image = ImageIO.read(systemFileFactory.fromNetDiskFile(netDiskFile).getInputStream());
        } catch (IOException e) {
            log.error("生成缩略图失败，读取图片失败: id=" + id, e);
            throw new SystemException();
        }
        int height, width;
        if(image.getHeight() > image.getWidth()){
            height = size;
            width = size * image.getWidth() / image.getHeight();
        }else{
            width = size;
            height = size * image.getHeight() / image.getWidth();
        }
        var thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = thumbnail.getGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        try {
            ImageIO.write(thumbnail, "webp", file);
        } catch (IOException e) {
            log.error("生成缩略图失败，写出图片失败: id=" + id, e);
            throw new SystemException();
        }
        try(var in = new FileInputStream(file)) {
            in.transferTo(response.getOutputStream());
        } catch (IOException e) {
            log.error("写出图片异常", e);
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("{id}")
    public void del(@PathVariable Long id){
        netDiskFileService.delete(id);
    }
}
