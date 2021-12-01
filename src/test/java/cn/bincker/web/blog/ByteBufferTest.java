package cn.bincker.web.blog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ByteBufferTest {
    private volatile boolean completed = false;
    private long size;
    private long process = 0;

    @Test
    void test() {
        var byteBuffer = ByteBuffer.allocate(1);
        byteBuffer.put(new byte[]{0x33, 0x44});
        System.out.println(byteBuffer.position());
        System.out.println(byteBuffer);
    }

    @Test
    void readFileTest() throws NoSuchAlgorithmException {
        final var byteBuffer = ByteBuffer.allocate(8172);
        new Thread(()->{
            FileChannel file;
            MessageDigest localDigest;
            try {
                file = FileChannel.open(Path.of("/home/bincker/Downloads/0256becc79354980848feb1e8484f27b.zip"), StandardOpenOption.READ);
                size = file.size();
                localDigest = MessageDigest.getInstance("sha256");
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            var localByteBuffer = ByteBuffer.allocate(8172);
            var len = 0;
            try {
                while ((len = file.read(localByteBuffer)) != -1){
                    process += len;
                    if(!localByteBuffer.hasRemaining()){
                        synchronized (this){
                            if(byteBuffer.hasRemaining()){
                                localByteBuffer.flip();
                                localDigest.update(localByteBuffer.slice());
                                byteBuffer.put(localByteBuffer);
                                localByteBuffer.compact();
                            }
                        }
                    }
                }
                localByteBuffer.flip();
                while (localByteBuffer.hasRemaining()){
                    synchronized (this){
                        if(byteBuffer.hasRemaining()){
                            localDigest.update(localByteBuffer.slice());
                            byteBuffer.put(localByteBuffer);
                        }
                    }
                }
                completed = true;
                System.out.println("local digest: " + byte2hex(localDigest.digest()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(()->{
            while (!completed){
                try {
                    Thread.sleep(1000);
                    System.out.println(process + "/" + size + "\t" + (int)(process * 1.0 / size * 100) + "%");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //计算
        var digest = MessageDigest.getInstance("sha256");
        while (!completed || byteBuffer.remaining() != byteBuffer.capacity()){
            synchronized (this){
                byteBuffer.flip();
                if(byteBuffer.hasRemaining()){
                    digest.update(byteBuffer);
                }
                byteBuffer.compact();
            }
        }
        byte[] sha256 = digest.digest();
        System.out.println(byte2hex(sha256));
    }

    @Test
    void byte2hexTest() {
        System.out.println(byte2hex(new byte[]{0x33, 0x77, (byte) 0xc1, 0x6d, 0x09}));
    }

    static final char[] digits = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
            'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
            'o' , 'p' , 'q' , 'r' , 's' , 't' ,
            'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    private String byte2hex(byte[] data){
        var result = new StringBuilder(data.length * 2);
        for (byte b : data) {
            result.append(digits[b >> 4 & 0x0f]).append(digits[b & 0x0f]);
        }
        return result.toString();
    }
}
