package cn.bincker.web.blog.base.entity;

import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.utils.FileUtils;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;

public class AliyunOssSystemFileImpl implements ISystemFile{
    private static final Logger log = LoggerFactory.getLogger(AliyunOssSystemFileImpl.class);

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
        return oss.putObject(bucketName, basePath + path, InputStream.nullInputStream()).getResponse().isSuccessful();
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
        if(transportComplete != 0) throw new SystemException();
        final ByteBuffer byteBuffer = ByteBuffer.allocate(8192);
        transportComplete = 1;
        final var out = new OutputStream() {
            private final ByteBuffer buffer = ByteBuffer.allocate(8192);

            @Override
            public void flush() throws EOFException {
                boolean wait = false;
                while (true){
                    if(transportComplete != 1) throw new EOFException();
                    if(wait){
                        while(true){
                            if(transportComplete != 1) throw new EOFException();
                            synchronized (AliyunOssSystemFileImpl.this){
                                if(byteBuffer.hasRemaining()) break;
                            }
                        }
                    }
                    synchronized (AliyunOssSystemFileImpl.this){
                        if(!byteBuffer.hasRemaining()){
                            wait = true;
                            continue;
                        }
                        buffer.flip();
                        byteBuffer.put(buffer);
                        buffer.compact();
                        break;
                    }
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
                buffer.put((byte) b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                if(transportComplete != 1) throw new EOFException();
                int index = 0, len;
                while (index < b.length && transportComplete == 1){
                    if(!buffer.hasRemaining()) flush();
                    len = Math.min(buffer.remaining(), b.length - index);
                    buffer.put(b, index, len);
                    index += len;
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if(transportComplete != 1) throw new EOFException();
                int cOff = off, cLen;
                while (cOff < off + len && transportComplete == 1){
                    if(!buffer.hasRemaining())
                        flush();
                    cLen = Math.min(buffer.remaining(), off + len - cOff);
                    log.info("write bytes to aliyun oss:" + cLen);
                    buffer.put(b, cOff, cLen);
                    cOff += cLen;
                }
            }
        };
        final var in = new InputStream() {
            /**
             * 等待写入
             */
            private void waitWrite() throws EOFException {
                while (transportComplete != 2) {
                    if (transportComplete != 1)
                        throw new EOFException();
                    synchronized (AliyunOssSystemFileImpl.this) {
                        byteBuffer.flip();
                        if (byteBuffer.hasRemaining()) {
                            byteBuffer.compact();
                            break;
                        }
                        byteBuffer.compact();
                    }
                }
            }

            @Override
            public int read() throws IOException {
                if(transportComplete != 1) return -1;
                byte result;
                boolean wait = false;
                while (true) {
                    if(wait) {
                        waitWrite();
                        if(!byteBuffer.hasRemaining()) return -1;
                    }
                    synchronized (AliyunOssSystemFileImpl.this) {
                        byteBuffer.flip();
                        if (!byteBuffer.hasRemaining()) {
                            wait = true;
                            byteBuffer.compact();
                            continue;
                        }
                        result = byteBuffer.get();
                        byteBuffer.compact();
                        break;
                    }
                }
                return result;
            }

            /**
             * 只读缓冲区中的数据
             */
            private int readAvailable(byte[] b, int off, int len){
                synchronized (AliyunOssSystemFileImpl.this){
                    byteBuffer.flip();
                    int l = Math.min(byteBuffer.remaining(), len);
                    if(l > 0) byteBuffer.get(b, off, l);
                    byteBuffer.compact();
                    return l;
                }
            }

            @Override
            public int read(byte[] b) throws IOException {
                if(transportComplete != 1) return -1;
                int i=0, l;
                boolean wait = false;
                while (i < b.length && transportComplete == 1){
                    if(wait) waitWrite();
                    synchronized (AliyunOssSystemFileImpl.this){
                        byteBuffer.flip();
                        if(!byteBuffer.hasRemaining()) {
                            wait = true;
                            byteBuffer.compact();
                            continue;
                        }
                        l = Math.min(byteBuffer.remaining(), b.length - i);
                        byteBuffer.get(b, i, l);
                        i += l;
                        byteBuffer.compact();
                    }
                }
                if(i < b.length && transportComplete == 2){
                    i += readAvailable(b, i, b.length - i);
                }
                if(i == 0 && transportComplete == 2) return -1;
                return i;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if(transportComplete != 1) return -1;
                int i=off, l;
                boolean wait = false;
                while (i < off + len && transportComplete == 1){
                    if(wait) waitWrite();
                    synchronized (AliyunOssSystemFileImpl.this){
                        byteBuffer.flip();
                        if(!byteBuffer.hasRemaining()) {
                            wait = true;
                            byteBuffer.compact();
                            continue;
                        }
                        l = Math.min(byteBuffer.remaining(), off + len - i);
                        byteBuffer.get(b, i, l);
                        i += l;
                        byteBuffer.compact();
                    }
                }
                if(i < b.length && transportComplete == 2){
                    i += readAvailable(b, i, b.length - i);
                }
                if(i - off == 0 && transportComplete == 2) return -1;
                return i - off;
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
        new Thread(()-> {
            try {
                oss.putObject(bucketName, basePath + path, in);
            }catch (OSSException | ClientException e){
                log.error("上传阿里云OSS文件[" + getPath() + "]失败:" + e.getMessage(), e);
            }
        }).start();
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
