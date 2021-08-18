package cn.bincker.web.blog.base.dto;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class BaseUserDto {
    @NotEmpty
    @Pattern(regexp = RegexpConstant.USERNAME_VALUE, message = "5-16位中文、英文字母、数字、下划线、中划线")
    private String username;
    private String password;
    private String headImg;
    @Email
    private String email;
    @Pattern(regexp = RegexpConstant.PHONE_NUM_VALUE, message = "无效手机号")
    private String phoneNum;
    private String biography;
    @URL(message = "无效地址")
    private String website;
    private String qqOpenId;
    private String wechatOpenId;
    private String github;
}
