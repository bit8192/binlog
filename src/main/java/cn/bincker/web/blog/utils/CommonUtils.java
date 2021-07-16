package cn.bincker.web.blog.utils;

import java.util.Stack;

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
     * 通配符*匹配, "*"字符匹配任何字符
     * @param pattern 匹配模式
     * @param target 匹配对象
     */
    public static boolean simpleMatch(String pattern, String target){
        if(target == null || pattern == null) return false;
        if(target.length() < pattern.length()) return false;
        var targetCharArr = target.toCharArray();
        var pCharArr = pattern.toCharArray();
        var matchStack = new Stack<Integer[]>();
        var matchIndex = 0;
        for (int i = 0; i < pCharArr.length; i++) {
            var pc = pCharArr[i];
            if(pc == '*'){
                //如果剩余字符数量小于模式字符数量，则表示无法匹配，进行回溯或者结束
                if(targetCharArr.length - matchIndex < pCharArr.length - i){
                    if(matchStack.empty()) return false;
                    var param = matchStack.pop();
                    i = param[0] - 1;
                    matchIndex = param[1];
                    continue;
                }
                matchIndex ++;//当前匹配，进行下一个字符的匹配
                matchStack.push(new Integer[]{i, matchIndex});//记录位置，用于回溯
            }else if(pc != targetCharArr[matchIndex]){
                //如果不匹配，那么判断是否有栈可以回溯，如果没有那么代表当前模式不匹配
                if(matchStack.empty()) {
                    return false;
                }else{
                    //回溯
                    var param = matchStack.pop();
                    i = param[0] - 1;//-1 是因为下次循环需要+1
                    matchIndex = param[1];
                }
            }else{
                matchIndex ++;
            }
        }
        return true;
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
