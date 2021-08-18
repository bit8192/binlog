package cn.bincker.web.blog.base.constant;

public class SessionKeyConstant {
    /**
     * Oauth2授权时存储的state
     */
    public static final String OAUTH2_AUTHORIZE_STATE = "SESSION_KEY_OAUTH2_AUTHORIZE_STATE";
    /**
     * Github的Oauth2授权时存储Github的用户名，注册时不用前端传回的用户名，以防撰改
     */
    public static final String OAUTH2_AUTHORIZE_GITHUB = "SESSION_KEY_OAUTH2_AUTHORIZE_GITHUB";
    /**
     * 存储QQ的Oauth2授权成功后存储的openid，用于注册时防止撰改、帐号绑定
     */
    public static final String OAUTH2_AUTHORIZE_QQ_OPENID = "OAUTH2_AUTHORIZE_QQ_OPENID";
}
