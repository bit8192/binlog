package cn.bincker.web.blog.base.constant;

public class RegexpConstant {
    /**
     * 匹配合法文件名
     */
    public static final String FILE_NAME = "[^/\\\\:*?\"<>|\\s]+";
    /**
     * 非法文件名字符
     */
    public static final String ILLEGAL_FILE_NAME_CHAR = "[/\\\\:*?\"<>|\\s]";
}
