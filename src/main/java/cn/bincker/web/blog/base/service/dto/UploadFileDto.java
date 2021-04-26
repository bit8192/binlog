package cn.bincker.web.blog.base.service.dto;

import lombok.Data;

@Data
public class UploadFileDto {
    private Long id;
    private String url;
    private String mediaType;
    private String suffix;
    private Long size;
    private String name;
}
