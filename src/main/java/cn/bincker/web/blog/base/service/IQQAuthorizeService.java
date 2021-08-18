package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.QQAccessToken;
import cn.bincker.web.blog.base.entity.QQUserInfo;

public interface IQQAuthorizeService extends IOauth2AuthorizeService<QQAccessToken, QQUserInfo> {
    String getOpenId(String accessToken);
}
