package cn.bincker.web.blog.netdisk.dto;

import lombok.Data;

import java.util.List;

@Data
public class NetDiskFileUploadFileDto {
    private Long parentId;
    private Boolean everyoneReadable = false;
    private Boolean everyoneWritable = false;
    private List<Long> readableUserList;
    private List<Long> writableUserList;
}
