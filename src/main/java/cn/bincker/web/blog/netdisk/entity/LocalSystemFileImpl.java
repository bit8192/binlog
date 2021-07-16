package cn.bincker.web.blog.netdisk.entity;

import cn.bincker.web.blog.utils.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

public class LocalSystemFileImpl implements ISystemFile {
    private final File file;

    public LocalSystemFileImpl(NetDiskFile netDiskFile) {
        this.file = new File(netDiskFile.getPath());
    }

    public LocalSystemFileImpl(String path){
        this.file = new File(path);
    }

    public LocalSystemFileImpl(String path, String child){
        this.file = new File(path, child);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean mkdir() {
        return file.mkdir();
    }

    @Override
    public boolean mkdirs() {
        return file.mkdirs();
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    @Override
    public boolean renameTo(String toPath) {
        return file.renameTo(new File(toPath));
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
