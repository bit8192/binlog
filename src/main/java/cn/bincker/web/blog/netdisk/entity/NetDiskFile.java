package cn.bincker.web.blog.netdisk.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class NetDiskFile extends AuditEntity {
    private String name;
    private String path;
    @Column(nullable = false)
    private Boolean isDirectory = true;

    @ManyToOne
    private UploadFile uploadFile;

    @ManyToOne
    private NetDiskFile parent;

    @OneToMany(mappedBy = "parent")
    private List<NetDiskFile> children;

    /**
     * 所有人
     */
    @ManyToOne(optional = false)
    private BaseUser possessor;

    /**
     * 任何人可读
     */
    @Column(nullable = false)
    private Boolean everyoneReadable = false;

    /**
     * 任何人可写
     */
    @Column(nullable = false)
    private Boolean everyoneWritable = false;

    /**
     * 可读用户
     */
    @ManyToMany
    private List<BaseUser> readableUserList;

    /**
     * 可写用户
     */
    @ManyToMany
    private List<BaseUser> writableUserList;
}
