package cn.bincker.web.blog.material.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class TagPostDto {
    @NotEmpty
    @Length(min = 2)
    private String title;
}
