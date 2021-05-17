package cn.bincker.web.blog.material.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class TagPutDto {
    @NotNull
    private Long id;
    @NotNull
    @Length(min = 2)
    private String title;
}
