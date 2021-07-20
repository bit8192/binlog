package cn.bincker.web.blog.netdisk.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.netdisk.enumeration.FileSystemTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class NetDiskFile extends AuditEntity {
    private String name;
    private String path;
    @Column(nullable = false)
    private Boolean isDirectory = true;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private FileSystemTypeEnum storageLocation;

    /**
     * 文件后缀，全用小写，方便查询
     */
    private String suffix;

    private String mediaType;

    @Min(0)
    private long size;

    private String sha256;

    @ManyToOne
    private NetDiskFile parent;

    /**
     * 所有父级节点
     */
    @Convert(converter = ParentsConverter.class)
    private List<Long> parents;

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
    private Set<BaseUser> readableUserList = new HashSet<>();

    /**
     * 可写用户
     */
    @ManyToMany
    private Set<BaseUser> writableUserList = new HashSet<>();

    public static class ParentsConverter implements AttributeConverter<List<Long>, String>{
        @Override
        public String convertToDatabaseColumn(List<Long> ids) {
            if(ids == null || ids.isEmpty()) return null;
            return "/" + ids.stream().map(String::valueOf).collect(Collectors.joining("/")) + "/";
        }

        @Override
        public List<Long> convertToEntityAttribute(String s) {
            if(!StringUtils.hasText(s)) return Collections.emptyList();
            return Stream.of(s.split("/")).filter(i->i.length() > 0).map(Long::valueOf).collect(Collectors.toList());
        }
    }
}
