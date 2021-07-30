package cn.bincker.web.blog.utils;

import cn.bincker.web.blog.base.exception.SystemException;
import org.springframework.util.StringUtils;

import java.io.File;
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

    /**
     * 拼接路径
     */
    public static String join(String ...paths){
        if(paths.length < 1) return "";
        var result = new StringBuilder(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            if(result.isEmpty()){//如果一开始就是空的，说明不是绝对路径，若后面有斜杠则去除
                if(paths[i].startsWith(File.separator)){
                    result.append(paths[i].substring(1));
                }else{
                    result.append(paths[i]);
                }
            }else if(result.charAt(result.length() - 1) == File.separatorChar) {//拼接的路径最后有斜杠
                if(paths[i].startsWith(File.separator)){
                    result.append(paths[i].substring(1));
                }else{
                    result.append(paths[i]);
                }
            }else{//拼接的路径最后没有斜杠
                if(paths[i].startsWith(File.separator)){
                    result.append(paths[i]);
                }else{
                    result.append(File.separatorChar).append(paths[i]);
                }
            }
        }
        return result.toString();
    }

    public static String getFileSuffix(String filename){
        var suffix = CommonUtils.getStringSuffix(filename, ".");
        if(!StringUtils.hasText(suffix)) return suffix;
        return "." + suffix;
    }
}
