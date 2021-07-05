package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.QQAccessToken;
import cn.bincker.web.blog.base.entity.QQUserInfo;

public interface IQQAuthorizeService {
    String getAuthorizeUrl(String redirectUrl, String state);
    QQAccessToken getAccessToken(String code, String redirectUrl);
    String getOpenId(String accessToken);
    QQUserInfo getUserInfo(String accessToken, String openId);
}
