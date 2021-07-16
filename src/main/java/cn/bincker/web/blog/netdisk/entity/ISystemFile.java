package cn.bincker.web.blog.netdisk.entity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ISystemFile {
    String getName();
    String getPath();
    boolean exists();
    boolean mkdir();
    boolean mkdirs();
    OutputStream getOutputStream() throws FileNotFoundException;
    boolean renameTo(String toPath);
    boolean delete();

    InputStream getInputStream() throws FileNotFoundException;
}
