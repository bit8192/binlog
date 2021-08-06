package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@EntityListeners(UserAuditingListener.class)
public class UploadFile extends BaseEntity{
    @JsonIgnore
    @NotNull
    private String path;

    @NotEmpty
    private String name;

    private String suffix;

    private String mediaType;

    @Min(0)
    private long size;

    @NotNull
    private String sha256;

    @JsonIgnore
    @ManyToOne
    @CreatedBy
    private BaseUser createdUser;

    private Boolean isPublic;
}
