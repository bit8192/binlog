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
    public static final String COMMENT_MEMBER_VALUE = "@(\\S+)($|\\s)";

    public static final Pattern COMMENT_MEMBER = Pattern.compile(COMMENT_MEMBER_VALUE);

    /**
     * 无效用户名
     */
    public static final String ILLEGAL_USERNAME_VALUE = "[^_\\-\\w\\u4e00-\\u9fa5]";

    public static final Pattern ILLEGAL_USERNAME = Pattern.compile(ILLEGAL_USERNAME_VALUE);

    /**
     * 回复消息开头
     */
    public static final String REPLY_CONTENT_PREFIX_VALUE = "^回复 @([_\\-\\w\\u4e00-\\u9fa5]) : ";

    public static final Pattern REPLY_CONTENT_PREFIX = Pattern.compile(REPLY_CONTENT_PREFIX_VALUE);

    /**
     * 图片文件
     */
    public static final String IMAGE_FILE_VALUE = "\\.((jpg)|(jpeg)|(bmp)|(png)|(icon)|(svg)|(webp))$";

    public static final Pattern IMAGE_FILE = Pattern.compile(IMAGE_FILE_VALUE, Pattern.CASE_INSENSITIVE);

    /**
     * 匹配URL的server host
     */
    public static final String URL_HOST_VALUE = "https?://([^/]+)";

    public static final Pattern URL_HOST = Pattern.compile(URL_HOST_VALUE);

    /**
     * 表情标题
     */
    public static final String EXPRESSION_TITLE_VALUE = "^[\\w-_\\u4e00-\\u9fa5]+$";

    public static final Pattern EXPRESSION_TITLE = Pattern.compile(EXPRESSION_TITLE_VALUE);

    /**
     * IP
     */
    public static final String IP_VALUE = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    public static final Pattern IP = Pattern.compile(IP_VALUE);
}
