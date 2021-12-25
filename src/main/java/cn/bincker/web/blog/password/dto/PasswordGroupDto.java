package cn.bincker.web.blog.password.dto;

import cn.bincker.web.blog.base.dto.valid.InsertValid;
import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
public class PasswordGroupDto {
    @NotNull(groups = {UpdateValid.class})
    @Null(groups = {InsertValid.class})
    private Long id;

    @NotEmpty
    private String title;

    private String remark;
}
