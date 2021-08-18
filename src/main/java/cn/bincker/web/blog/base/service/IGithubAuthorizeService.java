package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.entity.GithubAccessToken;
import cn.bincker.web.blog.base.entity.GithubUserInfo;

public interface IGithubAuthorizeService extends IOauth2AuthorizeService<GithubAccessToken, GithubUserInfo> {
}
