package cn.bincker.web.blog.utils;

import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@Component
public class SystemResourceUtils {
    private final MultipartProperties multipartProperties;

    public SystemResourceUtils(MultipartProperties multipartProperties) {
        this.multipartProperties = multipartProperties;
    }

    public File getUploadPath(String relativePath){
        File file;
        try {
            file = ResourceUtils.getFile(multipartProperties.getLocation() + File.separator + relativePath);
        } catch (FileNotFoundException e) {
            file = new File(multipartProperties.getLocation() + File.separator + relativePath);
            if(!file.mkdirs()) throw new RuntimeException("创建目录失败: path=" + file.getAbsolutePath());
        }
        return file;

    }
}
