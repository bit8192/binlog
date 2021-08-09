package cn.bincker.web.blog.utils;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class ResponseUtils {
    private static final DateFormat EXPIRES_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss X", Locale.US);
    /**
     * 设置缓存
     */
    public static void setCachePeriod(HttpServletResponse response, Duration period){
        response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + period.get(ChronoUnit.SECONDS));
        response.setHeader(HttpHeaders.EXPIRES, EXPIRES_DATE_FORMAT.format(new Date(System.currentTimeMillis() + period.toMillis())));
    }
}
