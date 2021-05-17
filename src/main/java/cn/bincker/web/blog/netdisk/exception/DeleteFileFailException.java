package cn.bincker.web.blog.netdisk.exception;

import cn.bincker.web.blog.netdisk.service.ISystemFile;
import lombok.Getter;

public class DeleteFileFailException extends RuntimeException{
    @Getter
    private final String tip;
    public DeleteFileFailException(ISystemFile file) {
        super("删除文件失败：path=" + file.getPath());
        tip = "删除文件[" + file.getName() + "]失败";
    }
}
