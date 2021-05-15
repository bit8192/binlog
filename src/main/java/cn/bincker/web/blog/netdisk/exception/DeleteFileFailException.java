package cn.bincker.web.blog.netdisk.exception;

import lombok.Getter;

import java.io.File;

public class DeleteFileFailException extends RuntimeException{
    @Getter
    private final String tip;
    public DeleteFileFailException(File file) {
        super("删除文件失败：path=" + file.getAbsolutePath());
        tip = "删除文件[" + file.getName() + "]失败";
    }
}
