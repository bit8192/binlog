package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.Oauth2AccessToken;
import cn.bincker.web.blog.base.entity.Oauth2UserInfo;

import javax.servlet.http.HttpServletRequest;

public interface IOauth2AuthorizeService<T extends Oauth2AccessToken, U extends Oauth2UserInfo> {
    String getAuthorizeUrl(String redirectUrl, String state);
    T getAccessToken(HttpServletRequest request, String code);
    U getUserInfo(String accessToken);
}
