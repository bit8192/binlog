package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.service.ISystemFile;
import cn.bincker.web.blog.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LocalSystemFileImpl implements ISystemFile {
    private static final Logger log = LoggerFactory.getLogger(LocalSystemFileImpl.class);
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
    public boolean renameTo(File toFile) {
        return file.renameTo(toFile);
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
