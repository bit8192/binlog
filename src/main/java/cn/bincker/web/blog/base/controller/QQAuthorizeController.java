package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.QQAccessToken;
import cn.bincker.web.blog.base.entity.QQUserInfo;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.service.IQQAuthorizeService;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@ConditionalOnBean(IQQAuthorizeService.class)
@Controller
@RequestMapping("${binlog.base-path}/authorize/qq")
public class QQAuthorizeController extends AbstractOauth2AuthorizeController<QQAccessToken, QQUserInfo>{
    private final String basePath;

    public QQAuthorizeController(@Value("${binlog.base-path}") String basePath, IQQAuthorizeService qqAuthorizeService, IBaseUserService userService, ISystemCacheService systemCacheService) {
        super(userService, systemCacheService, qqAuthorizeService);
        this.basePath = basePath;
    }

    @Override
    Optional<BaseUser> findUserByUserInfo(QQUserInfo userInfo) {
        return userService.findByQQOpenId(userInfo.getOpenId());
    }

    @Override
    void setUserInfo(BaseUser user, QQUserInfo oauth2UserInfo) {
        user.setQqOpenId(oauth2UserInfo.getOpenId());
    }

    @Override
    String getName() {
        return "github";
    }

    @Override
    String getRedirectUrl(HttpServletRequest request) {
        return RequestUtils.getRequestBaseUrl(request) + basePath + "/authorize/qq/notice";
    }

    @Override
    void configSessionBeforeRegister(HttpSession session, BaseUserVo vo) {
        session.setAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_QQ_OPENID, vo.getQqOpenId());
    }
}
