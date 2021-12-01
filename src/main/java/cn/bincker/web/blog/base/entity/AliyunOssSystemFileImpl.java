package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.utils.FileUtils;
import com.aliyun.oss.OSS;

import java.io.*;
import java.nio.ByteBuffer;

public class AliyunOssSystemFileImpl implements ISystemFile{
    private final OSS oss;
    private final String bucketName;
    private final String basePath;
    private final String path;
    private volatile int transportComplete = 0;//0 空闲，1 传输中，2 传输完成

    public AliyunOssSystemFileImpl(OSS oss, String bucketName, String basePath, String path) {
        this.oss = oss;
        this.bucketName = bucketName;
        this.basePath = basePath;
        this.path = path;
    }

    @Override
    public String getName() {
        return FileUtils.getFileName(path);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean exists() {
        return oss.doesObjectExist(bucketName, basePath + path);
    }

    @Override
    public boolean mkdir() {
        return oss.putObject(bucketName, basePath + path + "/", InputStream.nullInputStream()).getResponse().isSuccessful();
    }

    @Override
    public boolean mkdirs() {
        var dirStack = FileUtils.getDirectoryStack(basePath + path, '/');
        while (!dirStack.isEmpty()){
            var dir = dirStack.pop() + "/";
            if(!oss.doesObjectExist(bucketName, dir)){
                oss.putObject(bucketName, dir, InputStream.nullInputStream());
            }
        }
        return true;
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        final var byteBuffer = ByteBuffer.allocate(8192);
        transportComplete = 1;
        final var out = new OutputStream() {
            private final ByteBuffer buffer = ByteBuffer.allocate(8192);

            @Override
            public void flush() throws EOFException {
                synchronized (AliyunOssSystemFileImpl.this){
                    if(transportComplete != 1) throw new EOFException();
                    while(!byteBuffer.hasRemaining()) {
                        try {
                            AliyunOssSystemFileImpl.this.wait(10000);
                        } catch (InterruptedException e) {
                            throw new SystemException("阿里云上传文件传输失败", e);
                        }
                    }
                    buffer.flip();
                    byteBuffer.put(buffer);
                    buffer.compact();
                    AliyunOssSystemFileImpl.this.notify();
                }
            }

            @Override
            public void close() {
                transportComplete = 2;
            }

            @Override
            public void write(int b) throws IOException {
                if(transportComplete != 1) throw new EOFException();
                if(!buffer.hasRemaining()) flush();
                byteBuffer.put((byte) b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                if(transportComplete != 1) throw new EOFException();
                int index = 0, len;
                while (index < b.length && transportComplete == 1){
                    if(!buffer.hasRemaining()) flush();
                    len = Math.min(buffer.remaining(), b.length - index);
                    byteBuffer.put(b, index, len);
                    index += len;
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if(transportComplete != 1) throw new EOFException();
                int cOff = off, cLen;
                while (cOff < off + len && transportComplete == 1){
                    if(!buffer.hasRemaining()) flush();
                    cLen = Math.min(buffer.remaining(), off + len - cOff);
                    byteBuffer.put(b, cOff, cLen);
                    cOff += cLen;
                }
            }
        };
        final var in = new InputStream() {
            /**
             * 等待写入，要先flip
             * 线程不安全需要在外部加锁
             */
            private void waitWrite() throws EOFException {
                while (!byteBuffer.hasRemaining()){
                    if(transportComplete != 1) throw new EOFException();
                    try{
                        byteBuffer.compact();//等待写入
                        AliyunOssSystemFileImpl.this.wait(10000);
                    } catch (InterruptedException e) {
                        throw new SystemException("上传文件到阿里云失败", e);
                    }finally {
                        byteBuffer.flip();//再次准备读
                    }
                }
            }

            @Override
            public int read() throws IOException {
                if(transportComplete != 1) return -1;
                byte result;
                synchronized (AliyunOssSystemFileImpl.this){
                    byteBuffer.flip();
                    if(!byteBuffer.hasRemaining()) waitWrite();
                    result = byteBuffer.get();
                    AliyunOssSystemFileImpl.this.notify();
                }
                return result;
            }

            @Override
            public int read(byte[] b) throws IOException {
                if(transportComplete != 1) return -1;
                int off=0, len;
                while (off < b.length && transportComplete == 1){
                    synchronized (AliyunOssSystemFileImpl.this){
                        byteBuffer.flip();
                        if(!byteBuffer.hasRemaining()) waitWrite();
                        len = Math.min(byteBuffer.remaining(), b.length - off);
                        byteBuffer.get(b, off, len);
                        off += len;
                        AliyunOssSystemFileImpl.this.notify();
                    }
                }
                return off;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if(transportComplete != 1) return -1;
                int rOff=off, rLen;
                while (rOff < off + len && transportComplete == 1){
                    synchronized (AliyunOssSystemFileImpl.this){
                        byteBuffer.flip();
                        if(!byteBuffer.hasRemaining()) waitWrite();
                        rLen = Math.min(byteBuffer.remaining(), off + len - rOff);
                        byteBuffer.get(b, rOff, rLen);
                        rOff += rLen;
                        AliyunOssSystemFileImpl.this.notify();
                    }
                }
                return rOff - off;
            }

            @Override
            public int available() {
                if(transportComplete != 1) return -1;
                int result;
                synchronized (AliyunOssSystemFileImpl.this){
                    byteBuffer.flip();
                    result = byteBuffer.remaining();
                    byteBuffer.compact();
                }
                return result;
            }

            @Override
            public void close() {
                transportComplete = 0;
            }
        };
        new Thread(()-> oss.putObject(bucketName, basePath + path, in)).start();
        return out;
    }

    @Override
    public boolean renameTo(String toPath) {
        return oss.renameObject(bucketName, basePath + path, basePath + toPath).getResponse().isSuccessful();
    }

    @Override
    public boolean delete() {
        return oss.deleteObject(bucketName, basePath + path).getResponse().isSuccessful();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return oss.getObject(bucketName, basePath + path).getObjectContent();
    }
}
