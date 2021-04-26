package cn.bincker.web.blog.utils;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class CommonUtils {
    /**
     * 获取文本后缀
     * @param str 原文本
     * @param separator 分隔符
     */
    public static String getStringSuffix(String str, String separator){
        if(str == null || str.isEmpty()) return "";
        int index = str.lastIndexOf(separator);
        if(index < 0) return "";
        return str.substring(index + 1);
    }

    /**
     * 字节转十六进制字符
     * @param data 数据
     */
    public static String bytes2hex(byte[] data){
        StringBuilder result = new StringBuilder();
        for (byte datum : data) {
            result.append(byte2char((datum >> 4) & 0x0f));
            result.append(byte2char(datum & 0x0f));
        }
        return result.toString();
    }

    /**
     * 单字节转char
     */
    private static char byte2char(int b){
        if(b < 10) return (char) ('0' + b);
        return (char) ('a' + b - 10);
    }

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
}
