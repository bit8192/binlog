package cn.bincker.web.blog.command;

/**
 * 命令行工具
 */
public class CommandUtils {
    /**
     * 取命令行参数位置
     * @param args 命令行
     * @param paramName 参数名
     * @param subParamName 别名
     */
    public static int parameterIndexOf(String[] args, String paramName, String subParamName){
        for (int i = 0; i < args.length; i++) {
            String c = args[i];
            if(c.trim().equals(paramName) || c.trim().equals(subParamName)) return i;
        }
        return -1;
    }

    /**
     * 判断命令行中是否有指定参数
     * @param args 命令行
     * @param paramName 参数名称
     * @param subParamName 别名
     */
    public static boolean hasParameter(String[] args, String paramName, String subParamName){
        return parameterIndexOf(args, paramName, subParamName) > -1;
    }

    /**
     * 取文本参数，会跳过其他参数，如：-a -b param 这时取a的参数那么将会取到param
     * @param args 命令行
     * @param paramName 参数名称
     * @param subParamName 别名
     */
    public static String getStringParameter(String[] args, String paramName, String subParamName){
        int index = parameterIndexOf(args, paramName, subParamName);
        if(index < 0) return "";
        if(index >= args.length - 1) return "";
        for (int i = index + 1; i < args.length; i++) {
            if(!args[i].startsWith("-")) return args[i];
        }
        return "";
    }
}
