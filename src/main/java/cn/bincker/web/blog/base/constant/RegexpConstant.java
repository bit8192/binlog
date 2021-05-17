package cn.bincker.web.blog.base.constant;

import java.util.regex.Pattern;

public class RegexpConstant {
    /**
     * 匹配合法文件名
     */
    public static final String FILE_NAME_VALUE = "[^/\\\\:*?\"<>|\\s]+";

    /**
     * 非法文件名字符
     */
    public static final String ILLEGAL_FILE_NAME_CHAR_VALUE = "[/\\\\:*?\"<>|\\s]";

    /**
     * 匹配markdown中的图片
     */
    public static final String MARKDOWN_IMAGE_VALUE = "!\\[[^]]+]\\(([^)\s]+)\s?(.*?)\\)";

    public static final Pattern MARKDOWN_IMAGE = Pattern.compile(MARKDOWN_IMAGE_VALUE);
}
