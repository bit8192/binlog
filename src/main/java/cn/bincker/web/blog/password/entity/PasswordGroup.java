package cn.bincker.web.blog.password.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * 密码分组
 */
@Entity
@Table(name = "password_group")
@Getter
@Setter
public class PasswordGroup extends AuditEntity {

    private String title;

    private String remark;

    /**
     * 使用次数,排序使用
     */
    private Long useTimes = 0L;

    @OneToMany(mappedBy = "passwordGroup")
    @JsonIgnore
    private List<PasswordInfo> passwordInfoList;
}
