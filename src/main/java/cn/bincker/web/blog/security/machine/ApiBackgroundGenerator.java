package cn.bincker.web.blog.security.machine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ApiBackgroundGenerator implements IBackgroundGenerator{
    private static final String url = "https://www.dmoe.cc/random.php";

    private final Random random = new Random();

    @Override
    public Image generator(int width, int height) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("accept", "image/*");
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            Image image;
            try(InputStream inputStream = connection.getInputStream()){
                image = ImageIO.read(inputStream);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
