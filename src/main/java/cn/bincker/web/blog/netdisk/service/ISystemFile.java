package cn.bincker.web.blog.netdisk.service;

import java.io.File;
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
    boolean renameTo(File toFile);
    boolean delete();

    InputStream getInputStream() throws FileNotFoundException;
}
