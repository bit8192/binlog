package cn.bincker.web.blog.netdisk.exception;

import java.io.File;

/**
 * 创建目录失败异常
 */
public class MakeDirectoryFailException extends RuntimeException{
    public MakeDirectoryFailException(File file) {
        super("创建目录失败: absolutePath=" + file.getAbsolutePath());
    }
}
