package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.entity.converter.RoleConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class BaseUser extends BaseEntity {
    @NotEmpty
    @Column(nullable = false, unique = true)
    private String username;

    private String nickname;

    @NotNull
    @Column(nullable = false)
    @JsonIgnore
    private String encodedPasswd;

    private String headImg;

    private String email;

    private String phoneNum;

    @Convert(converter = RoleConverter.class)
    private Set<Role> roles = new HashSet<>();

    @Column(columnDefinition = "tinyint(1) default false not null")
    @ColumnDefault("false")
    private Boolean locked;
}
