package cn.bincker.web.blog.netdisk.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.BaseUser;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class NetDiskFile extends AuditEntity {
    private String name;
    private String path;
    @Column(nullable = false)
    private Boolean isDirectory = true;

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
    @ToString.Exclude
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
    @ToString.Exclude
    private Set<BaseUser> readableUserList = new HashSet<>();

    /**
     * 可写用户
     */
    @ManyToMany
    @ToString.Exclude
    private Set<BaseUser> writableUserList = new HashSet<>();

    public static class ParentsConverter implements AttributeConverter<List<Long>, String>{
        @Override
        public String convertToDatabaseColumn(List<Long> ids) {
            if(ids == null || ids.isEmpty()) return null;
            return "/" + ids.stream().map(String::valueOf).collect(Collectors.joining("/")) + "/";
        }

        @Override
        public List<Long> convertToEntityAttribute(String s) {
            if(!StringUtils.hasText(s)) return new ArrayList<>();
            return Stream.of(s.split("/")).filter(i->i.length() > 0).map(Long::valueOf).collect(Collectors.toList());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        NetDiskFile that = (NetDiskFile) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return 552188326;
    }
}
