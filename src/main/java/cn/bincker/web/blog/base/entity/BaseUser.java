package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.entity.converter.RoleConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class BaseUser extends BaseEntity {
    @NotEmpty
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String encodedPasswd;

    private String headImg;

    private String email;

    private String phoneNum;

    /**
     * 个人简介
     */
    private String biography;

    /**
     * 个人网站
     */
    private String website;

    @Convert(converter = RoleConverter.class)
    private Set<Role> roles = new HashSet<>();

    @Column
    @ColumnDefault("false")
    private Boolean locked = false;

    @Column(unique = true)
    private String qqOpenId;

    @Column(unique = true)
    private String wechatOpenId;

    @Column(unique = true)
    private String github;
}
