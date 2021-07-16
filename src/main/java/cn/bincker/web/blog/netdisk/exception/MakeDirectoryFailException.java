package cn.bincker.web.blog.netdisk.exception;

import cn.bincker.web.blog.netdisk.entity.ISystemFile;

/**
 * 创建目录失败异常
 */
public class MakeDirectoryFailException extends RuntimeException{
    public MakeDirectoryFailException(ISystemFile file) {
        super("创建目录失败: path=" + file.getPath());
    }
}
