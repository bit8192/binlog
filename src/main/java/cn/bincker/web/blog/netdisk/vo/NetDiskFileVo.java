package cn.bincker.web.blog.netdisk.vo;

import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class NetDiskFileVo {
    private Long id;

    private String name;

    private Boolean isDirectory;

    private Long size = 0L;

    private Date createdDate;

    private Date lastModifiedDate;

    private BaseUserVo possessor;

    private BaseUserVo createdUser;

    private BaseUserVo lastModifiedUser;

    private Boolean everyoneReadable;

    private Boolean everyoneWritable;

    private List<BaseUserVo> readableUserList;

    private List<BaseUserVo> writableUserList;

    private Boolean readable;

    private Boolean writable;

    public NetDiskFileVo() {
    }

    public NetDiskFileVo(NetDiskFile netDiskFile) {
        this.id = netDiskFile.getId();
        this.name = netDiskFile.getName();
        this.isDirectory = netDiskFile.getIsDirectory();
        this.size = netDiskFile.getSize();
        this.createdDate = netDiskFile.getCreatedDate();
        this.lastModifiedDate = netDiskFile.getLastModifiedDate();
        if(netDiskFile.getPossessor() != null) this.possessor = new BaseUserVo(netDiskFile.getPossessor());
        if(netDiskFile.getCreatedUser() != null) this.createdUser = new BaseUserVo(netDiskFile.getCreatedUser());
        if(netDiskFile.getLastModifiedUser() != null) this.lastModifiedUser = new BaseUserVo(netDiskFile.getLastModifiedUser());
        this.everyoneReadable = netDiskFile.getEveryoneReadable();
        this.everyoneWritable = netDiskFile.getEveryoneWritable();
        this.readableUserList = netDiskFile.getReadableUserList().stream().map(BaseUserVo::new).collect(Collectors.toList());
        this.writableUserList = netDiskFile.getWritableUserList().stream().map(BaseUserVo::new).collect(Collectors.toList());
    }
}
