package cn.bincker.web.blog.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ResponseUtils {
    private static final Logger log = LoggerFactory.getLogger(ResponseUtils.class);
    private static final DateFormat WEB_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
    static {
        WEB_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 设置强缓存
     */
    public static void setCachePeriod(HttpServletResponse response, Duration period){
        response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + period.get(ChronoUnit.SECONDS));
        response.setHeader(HttpHeaders.PRAGMA, "Pragma");
        response.setHeader(HttpHeaders.EXPIRES, WEB_DATE_FORMAT.format(new Date(System.currentTimeMillis() + period.toMillis())));
    }

    /**
     * 检测缓存时间, 如果命中缓存返回true，并设置状态码，否则会设置LastModified响应头
     */
    public static boolean checkLastModified(HttpServletRequest request, HttpServletResponse response, Date lastModified){
        if(lastModified == null) return false;
        try {
            var modifiedTimeStr = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if(StringUtils.hasText(modifiedTimeStr)) {
                var requestModifiedTime = WEB_DATE_FORMAT.parse(modifiedTimeStr);
                if (requestModifiedTime.getTime() / 1000 >= lastModified.getTime() / 1000) {//忽略毫秒级误差
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return true;
                }
            }
            response.setHeader(HttpHeaders.LAST_MODIFIED, WEB_DATE_FORMAT.format(lastModified));
            return false;
        } catch (ParseException e) {
            log.error("检测LastModified缓存时时间解析异常", e);
            return false;
        }
    }

    /**
     * 检测ETag缓存
     */
    public static boolean checkETag(HttpServletRequest request, HttpServletResponse response, String tag){
        if(!StringUtils.hasText(tag)) return false;
        var requestTag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if(StringUtils.hasText(requestTag)){
            if(tag.equals(requestTag)){
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return true;
            }
        }
        response.setHeader(HttpHeaders.ETAG, tag);
        return false;
    }
}
