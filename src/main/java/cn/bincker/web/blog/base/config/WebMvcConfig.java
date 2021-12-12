package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.interceptor.RequestLogInterceptor;
import cn.bincker.web.blog.base.interceptor.RobotInterceptor;
import cn.bincker.web.blog.base.vo.SystemProfileVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import java.util.List;

@Configuration
@Log4j2
public class WebMvcConfig implements WebMvcConfigurer {
    private final BaseUserArgumentResolver baseUserArgumentResolver;
    private final RequestLogInterceptor requestLogInterceptor;
    private final ThymeleafViewResolver thymeleafViewResolver;
    private final SystemProfile systemProfile;
    private final RobotInterceptor robotInterceptor;

    public WebMvcConfig(BaseUserArgumentResolver baseUserArgumentResolver, RequestLogInterceptor requestLogInterceptor, ThymeleafViewResolver thymeleafViewResolver, SystemProfile systemProfile, RobotInterceptor robotInterceptor) {
        this.baseUserArgumentResolver = baseUserArgumentResolver;
        this.requestLogInterceptor = requestLogInterceptor;
        this.thymeleafViewResolver = thymeleafViewResolver;
        this.systemProfile = systemProfile;
        this.robotInterceptor = robotInterceptor;
    }

    @Autowired
    public void setThymeleafStaticVariable(SystemProfile profile, QQAuthorizeConfigProperties qqAuthorizeConfigProperties, GithubAuthorizeConfigProperties githubAuthorizeConfigProperties){
        var vo = new SystemProfileVo(profile);
        vo.setUseQQAuthorize(qqAuthorizeConfigProperties.isUse());
        vo.setUseGithubAuthorize(githubAuthorizeConfigProperties.isUse());
        thymeleafViewResolver.addStaticVariable("profile", vo);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(requestLogInterceptor);
        registry.addInterceptor(robotInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(baseUserArgumentResolver);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(systemProfile.getApiPath(), clazz->clazz.getAnnotation(ApiController.class) != null);
    }
}
