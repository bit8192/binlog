package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.interceptor.RequestLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final BaseUserArgumentResolver baseUserArgumentResolver;
    private final RequestLogInterceptor requestLogInterceptor;
    private final ThymeleafViewResolver thymeleafViewResolver;

    public WebMvcConfig(BaseUserArgumentResolver baseUserArgumentResolver, RequestLogInterceptor requestLogInterceptor, ThymeleafViewResolver thymeleafViewResolver) {
        this.baseUserArgumentResolver = baseUserArgumentResolver;
        this.requestLogInterceptor = requestLogInterceptor;
        this.thymeleafViewResolver = thymeleafViewResolver;
    }

    @Autowired
    public void setThymeleafStaticVariable(SystemProfile profile){
        thymeleafViewResolver.addStaticVariable("profile", profile);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(requestLogInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(baseUserArgumentResolver);
    }
}
