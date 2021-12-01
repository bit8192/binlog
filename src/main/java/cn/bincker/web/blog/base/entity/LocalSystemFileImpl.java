package cn.bincker.web.blog.base.entity;

import java.io.*;

public class LocalSystemFileImpl implements ISystemFile {
    private final String basePath;
    private final String path;
    private final File file;

    public LocalSystemFileImpl(String basePath, String path){
        this.basePath = basePath;
        this.path = path;
        this.file = new File(basePath, path);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getPath() {
        return path;
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
        return file.renameTo(new File(basePath, toPath));
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
