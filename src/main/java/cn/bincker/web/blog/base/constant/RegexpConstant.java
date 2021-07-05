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

    /**
     * 匹配评论中@的用户
     */
    public static final String COMMENT_MEMBER_VALUE = "(^|\\s)@(\\S+)($|\\s)";

    public static final Pattern COMMENT_MEMBER = Pattern.compile(COMMENT_MEMBER_VALUE);

    public static final String ILLEGAL_USERNAME_VALUE = "[^_\\-\\d\\w\\u4e00-\\u9fa5]";

    public static final Pattern ILLEGAL_USERNAME = Pattern.compile(ILLEGAL_USERNAME_VALUE);
}
