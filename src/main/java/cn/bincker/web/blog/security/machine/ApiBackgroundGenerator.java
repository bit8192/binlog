package cn.bincker.web.blog.security.machine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 通过api获取图片，这个只是临时用的，实际使用起来速度很慢
 */
public class ApiBackgroundGenerator extends IBackgroundGenerator{
    private static final String url = "https://www.dmoe.cc/random.php";

    @Override
    public Image getRawBackground() throws IOException{
        HttpURLConnection connection;
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("accept", "image/*");
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        Image image;
        try(InputStream inputStream = connection.getInputStream()){
            image = ImageIO.read(inputStream);
        }
        return image;
    }
}
