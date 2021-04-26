package cn.bincker.web.blog.security.machine;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WhiteBackgroundGenerator implements IBackgroundGenerator{
    @Override
    public Image generator(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        return image;
    }
}
