package cn.bincker.web.blog.netdisk.vo;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import lombok.Data;

import java.util.Date;

@Data
public class NetDiskFileListVo {
    private Long id;

    private String name;

    private Boolean isDirectory;

    private String mediaType;

    private Long size;

    private Date createdDate;

    private Date lastModifiedDate;

    private Long childrenNum;

    public NetDiskFileListVo(NetDiskFile file) {
        this.id = file.getId();
        this.name = file.getName();
        this.isDirectory = file.getIsDirectory();
        this.mediaType = file.getMediaType();
        this.size = file.getSize();
        this.createdDate = file.getCreatedDate();
        this.lastModifiedDate = file.getLastModifiedDate();
    }
}
