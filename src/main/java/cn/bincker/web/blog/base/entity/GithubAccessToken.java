package cn.bincker.web.blog.base.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubAccessToken implements Oauth2AccessToken{
    @JsonProperty("access_token")
    private String accessToken;
    private String scope;
    @JsonProperty("token_token")
    private String tokenType;
}
