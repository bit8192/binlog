package cn.bincker.web.blog.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
    public static byte[] sha256(InputStream in) throws NoSuchAlgorithmException, IOException {
        var digest = MessageDigest.getInstance("SHA-256");
        var buff = new byte[8192];
        var len = 0;
        while ((len = in.read(buff)) > 0){
            digest.update(buff, 0, len);
        }
        return digest.digest();
    }

    public static String sha256Hex(InputStream in) throws NoSuchAlgorithmException, IOException {
        return CommonUtils.bytes2hex(sha256(in));
    }
}
