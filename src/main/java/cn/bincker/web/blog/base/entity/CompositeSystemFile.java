package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.exception.SystemException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CompositeSystemFile implements ISystemFile{
    private List<ISystemFile> files;

    public CompositeSystemFile(List<ISystemFile> files) {
        this.files = files;
    }

    @Override
    public String getName() {
        return files.stream().findFirst().map(ISystemFile::getName).orElseThrow();
    }

    @Override
    public String getPath() {
        return files.stream().findFirst().map(ISystemFile::getPath).orElseThrow();
    }

    @Override
    public boolean exists() {
        return files.stream().allMatch(ISystemFile::exists);
    }

    @Override
    public boolean mkdir() {
        return files.stream().allMatch(ISystemFile::mkdir);
    }

    @Override
    public boolean mkdirs() {
        return files.stream().allMatch(ISystemFile::mkdirs);
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        if(files.size() > 1) throw new SystemException("无法同时操作多个数据源");
        if(files.size() < 1) throw new FileNotFoundException();
        return files.get(0).getOutputStream();
    }

    @Override
    public boolean renameTo(String toPath) {
        return files.stream().allMatch(f->f.renameTo(toPath));
    }

    @Override
    public boolean delete() {
        return files.stream().allMatch(ISystemFile::delete);
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        if(files.size() > 1) throw new SystemException("无法同时操作多个数据源");
        if(files.size() < 1) throw new FileNotFoundException();
        return files.get(0).getInputStream();
    }
}
