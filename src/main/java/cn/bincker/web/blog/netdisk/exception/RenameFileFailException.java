package cn.bincker.web.blog.netdisk.exception;

public class RenameFileFailException extends RuntimeException{
    public RenameFileFailException(String fromPath, String toPath) {
        super("重命名失败：fromPath=" + fromPath + "\ttoPath=" + toPath);
    }
}
