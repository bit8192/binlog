package cn.bincker.web.blog.netdisk.service.vo;

import lombok.Data;

@Data
public class NetDiskFileVo {
    private Long id;
    private String name;
    private Boolean isDirectory;

    public NetDiskFileVo() {
    }

    public NetDiskFileVo(Long id, String name, Boolean isDirectory) {
        this.id = id;
        this.name = name;
        this.isDirectory = isDirectory;
    }
}
