package cn.bincker.web.blog.security.machine;

import cn.bincker.web.blog.base.exception.SystemException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public abstract class IBackgroundGenerator {
    protected Random random = new Random();

    Image generator(int width, int height){
        Image image;
        try {
            image = getRawBackground();
        }catch (IOException e){
            throw new SystemException("读取背景图片失败", e);
        }
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
        Graphics2D graphics = result.createGraphics();

        //计算缩放比值
        double scale = 1;
        if(imageWidth < width){
            scale = width * 1.0 / imageWidth;
            if(imageHeight < height){
                double tmpScale = height * 1.0 / imageHeight;
                if(tmpScale < scale) scale = tmpScale;
            }
        }else if(imageWidth > width * 2){
            scale = 1.0 / (imageWidth * 1.0 / (width * 2));
            if(height / scale > imageHeight){
                scale = 1.0 / (imageHeight * 1.0 / (height * 2));
            }
        }
        //缩放后的高宽
        imageWidth = (int) (imageWidth * scale);
        imageHeight = (int) (imageHeight * scale);

        graphics.scale(scale, scale);
        graphics.drawImage(image, (int)(-(imageWidth - width) * random.nextDouble()), (int)(-(imageHeight - height) * random.nextDouble()), null);
        graphics.dispose();
        return result;
    }

    protected abstract Image getRawBackground() throws IOException;
}
