package cn.bincker.web.blog.utils;

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
}
