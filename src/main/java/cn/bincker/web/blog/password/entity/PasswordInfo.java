package cn.bincker.web.blog.password.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public class PasswordInfo extends AuditEntity {
    private String title;

    private String username;

    @Column(columnDefinition = "text", nullable = false)
    private String encodedPassword;

    private String url;

    @Column(columnDefinition = "text")
    private String remark;

    @ManyToOne
    private PasswordGroup passwordGroup;

    /**
     * 加密备注
     */
    private Boolean encryptionRemark;

    private Long useTimes = 0L;
}
