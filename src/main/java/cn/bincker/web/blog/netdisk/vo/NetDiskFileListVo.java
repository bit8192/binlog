package cn.bincker.web.blog.netdisk.vo;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import lombok.Data;

import java.util.Date;

@Data
public class NetDiskFileListVo {
    private Long id;

    private String name;

    private Boolean isDirectory;

    private Long size = 0L;

    private Date createdDate;

    private Date lastModifiedDate;

    public NetDiskFileListVo() {
    }

    public NetDiskFileListVo(Long id, String name, Boolean isDirectory, Long size, Date createdDate, Date lastModifiedDate) {
        this.id = id;
        this.name = name;
        this.isDirectory = isDirectory;
        this.size = size;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public NetDiskFileListVo(NetDiskFile file, Long size) {
        this.id = file.getId();
        this.name = file.getName();
        this.isDirectory = file.getIsDirectory();
        this.size = size;
        this.createdDate = file.getCreatedDate();
        this.lastModifiedDate = file.getLastModifiedDate();
    }
}
