package cn.bincker.web.blog.netdisk.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.BaseEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.entity.converter.LongArrayConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    /**
     * 所有父级节点
     */
    @Convert(converter = LongArrayConverter.class)
    private Long[] parents = new Long[]{};

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
    private Set<BaseUser> readableUserList = Collections.emptySet();

    /**
     * 可写用户
     */
    @ManyToMany
    private Set<BaseUser> writableUserList = Collections.emptySet();
}
