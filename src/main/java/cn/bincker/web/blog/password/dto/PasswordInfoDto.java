package cn.bincker.web.blog.password.dto;

import cn.bincker.web.blog.base.dto.valid.InsertValid;
import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
public class PasswordInfoDto {
    @NotNull(groups = {UpdateValid.class})
    @Null(groups = {InsertValid.class})
    private Long id;

    @NotNull
    private Long passwordGroupId;

    @NotEmpty
    private String title;

    private String username;

    @NotEmpty
    private String encodedPassword;

    private String url;

    private String remark;

    private Boolean encryptionRemark;
}
