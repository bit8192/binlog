package cn.bincker.web.blog.security.machine;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * 本地背景生成器
 */
public class LocalBackgroundGenerator extends IBackgroundGenerator implements FileFilter {
    private static final Logger log = LoggerFactory.getLogger(LocalBackgroundGenerator.class);
    private final File imagePath;

    public LocalBackgroundGenerator(File imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    protected Image getRawBackground() throws IOException {
        var imageFiles = imagePath.listFiles(this);
        if(imageFiles == null || imageFiles.length < 1){
            log.error("无法生成背景，图片文件夹没有可用的图片\tpath=" + imagePath);
            throw new SystemException();
        }
        var imageFile = imageFiles[random.nextInt(imageFiles.length)];
        return ImageIO.read(imageFile);
    }

    /**
     * 过滤图片文件
     */
    @Override
    public boolean accept(File file) {
        if(file.isDirectory() || !file.exists() || !file.canRead()) return false;
        return RegexpConstant.IMAGE_FILE.matcher(file.getName()).find();
    }
}
