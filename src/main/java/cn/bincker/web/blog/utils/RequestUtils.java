package cn.bincker.web.blog.utils;

import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RequestUtils {

    /**
     * 获取请求ip
     */
    public static String getRequestIp(HttpServletRequest request){
        String ip = request.getHeader("X-Real-IP");
        if(!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("X-Forwarded-For");
        if(!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("Proxy-Client-Ip");
        if(!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("WL-Proxy-Client-Ip");
        if(!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        if(StringUtils.hasText(ip) && ip.contains(",")){
            String[] ips = ip.split(",");
            if(ips.length < 1) return "";
            return ips[0];
        }
        return ip;
    }

    /**
     * 获取请求基础Url
     * 如：http://baidu.com/test/test 会得到 http://baidu.com
     */
    public static String getRequestBaseUrl(HttpServletRequest request){
        int port = request.getServerPort();
        if(port == 80 || port == 443){
            return request.getScheme() + "://" + request.getServerName() + request.getContextPath();
        }else{
            return request.getScheme() + "://" + request.getServerName() + ":" + port + request.getContextPath();
        }
    }

    /**
     * 获取Cookie
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name){
        var cookies = request.getCookies();
        if(cookies == null) return Optional.empty();
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(name)){
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }
}
