package cn.bincker.web.blog.netdisk.service.dto;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
public class NetDiskFilePostDto {
    @NotEmpty
    @Pattern(regexp = RegexpConstant.FILE_NAME)
    @Length(max = 255)
    private String name;
    private Long parentId;
    private Boolean everyoneReadable;
    private Boolean everyoneWritable;
    private List<Long> readableUserList;
    private List<Long> writableUserList;
}
