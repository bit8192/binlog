package cn.bincker.web.blog.base.entity;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class QQUserInfo implements Oauth2UserInfo {
    private Integer ret;
    private String msg;
    private String nickname;
    private String figureurl;
    private String figureurl_1;
    private String figureurl_2;
    private String figureurl_qq_1;
    private String figureurl_qq_2;
    private String gender;
    private String openId;

    @Override
    public String getUsername() {
        return nickname;
    }

    @Override
    public String getHeadImg() {
        if(StringUtils.hasText(figureurl_qq_2)){
            return figureurl_qq_2;
        }else{
            return figureurl_qq_1;
        }
    }
}
