package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.GithubAccessToken;
import cn.bincker.web.blog.base.entity.GithubUserInfo;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.service.IGithubAuthorizeService;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@ConditionalOnBean(IGithubAuthorizeService.class)
@RequestMapping("/authorize/github")
public class GithubAuthorizeController extends AbstractOauth2AuthorizeController<GithubAccessToken, GithubUserInfo> {

    protected GithubAuthorizeController(IBaseUserService userService, ISystemCacheService systemCacheService, IGithubAuthorizeService githubAuthorizeService) {
        super(userService, systemCacheService, githubAuthorizeService);
    }

    @Override
    String getRedirectUrl(HttpServletRequest request) {
        return RequestUtils.getRequestBaseUrl(request) + "/authorize/github/notice";
    }

    @Override
    Optional<BaseUser> findUserByUserInfo(GithubUserInfo userInfo) {
        return userService.findByGithub(userInfo.getUsername());
    }

    @Override
    void setUserInfo(BaseUser user, GithubUserInfo oauth2UserInfo) {
        user.setEmail(oauth2UserInfo.getEmail());
        user.setWebsite(oauth2UserInfo.getBlog());
        user.setBiography(oauth2UserInfo.getBio());
        user.setGithub(oauth2UserInfo.getLogin());
    }

    @Override
    String getName() {
        return "github";
    }

    @Override
    void configSessionBeforeRegister(HttpSession session, BaseUserVo vo) {
        session.setAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_GITHUB, vo.getGithub());
    }
}
