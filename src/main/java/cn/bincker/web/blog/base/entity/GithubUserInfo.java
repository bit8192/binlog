package cn.bincker.web.blog.base.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubUserInfo implements Oauth2UserInfo{
    private String login;
    private Long id;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String email;
    private String blog;
    private String bio;

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public String getHeadImg() {
        return avatarUrl;
    }
}
