package cn.bincker.web.blog.netdisk.exception;

import cn.bincker.web.blog.base.entity.ISystemFile;
import lombok.Getter;

public class DeleteFileFailException extends RuntimeException{
    @Getter
    private final String tip;
    public DeleteFileFailException(ISystemFile file) {
        this(file.getPath());
    }

    public DeleteFileFailException(String path) {
        super("删除文件失败：path=" + path);
        tip = "删除文件[" + path + "]失败";
    }

    public DeleteFileFailException(ISystemFile file, Throwable throwable) {
        this(file.getPath(), throwable);
    }

    public DeleteFileFailException(String path, Throwable throwable) {
        super("删除文件失败：path=" + path, throwable);
        tip = "删除文件[" + path + "]失败";
    }
}
