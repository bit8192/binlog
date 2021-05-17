package cn.bincker.web.blog.netdisk.service.vo;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.service.vo.BaseUserVo;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import lombok.Data;

import java.util.Date;
import java.util.Optional;

@Data
public class NetDiskFileVo {
    private Long id;
    private String name;
    private Boolean isDirectory;
    private Long size = 0L;
    /**
     * 子节点数量
     */
    private Date createdDate;
    private Date lastModifiedDate;
    private BaseUserVo possessor;
    private BaseUserVo createdUser;
    private BaseUserVo lastModifiedUser;
    private Long childrenNum;

    public NetDiskFileVo() {
    }

    public NetDiskFileVo(NetDiskFile netDiskFile) {
        this(netDiskFile, 0L);
    }

    public NetDiskFileVo(NetDiskFile netDiskFile, Long childrenNum) {
        this.id = netDiskFile.getId();
        this.name = netDiskFile.getName();
        this.isDirectory = netDiskFile.getIsDirectory();
        if(netDiskFile.getUploadFile() != null) {
            this.size = netDiskFile.getUploadFile().getSize();
        }
        this.createdDate = netDiskFile.getCreatedDate();
        this.lastModifiedDate = netDiskFile.getLastModifiedDate();
        if(netDiskFile.getPossessor() != null) this.possessor = new BaseUserVo(netDiskFile.getPossessor());
        if(netDiskFile.getCreatedUser() != null) this.createdUser = new BaseUserVo(netDiskFile.getCreatedUser());
        if(netDiskFile.getLastModifiedUser() != null) this.lastModifiedUser = new BaseUserVo(netDiskFile.getLastModifiedUser());
        this.childrenNum = childrenNum;
    }
}
