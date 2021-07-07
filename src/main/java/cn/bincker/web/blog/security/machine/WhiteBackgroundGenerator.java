package cn.bincker.web.blog.security.machine;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 空白的背景生成器
 */
public class WhiteBackgroundGenerator extends IBackgroundGenerator{
    private final int width;
    private final int height;

    public WhiteBackgroundGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Image getRawBackground() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        return image;
    }
}
