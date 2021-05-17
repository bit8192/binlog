package cn.bincker.web.blog.utils;

import cn.bincker.web.blog.base.exception.SystemException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
    private static final Pattern PATTERN_SERIAL_FILE_NAME = Pattern.compile("(\\S*?)(-\\d+)?((\\.[\\w_]+)*)$");

    /**
     * 获取下一个序列文件名
     * 如：test-1.txt  test-2.txt  test-3.txt
     * @param filename 文件名
     */
    public static String nextSerialFileName(String filename){
        Matcher matcher = PATTERN_SERIAL_FILE_NAME.matcher(filename);
        if(!matcher.find()) throw new SystemException("无效文件名 filename=" + filename);
        int index = 1;
        try{
            String indexStr = matcher.group(2);
            if(indexStr != null){
                index = Integer.parseInt(indexStr.substring(1)) + 1;
            }
        }catch (IllegalStateException ignore) {}
        return matcher.replaceFirst("$1-" + index + "$3");
    }
}
