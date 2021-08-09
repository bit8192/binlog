package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.interceptor.RequestLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final BaseUserArgumentResolver baseUserArgumentResolver;
    private final RequestLogInterceptor requestLogInterceptor;

    public WebMvcConfig(BaseUserArgumentResolver baseUserArgumentResolver, RequestLogInterceptor requestLogInterceptor) {
        this.baseUserArgumentResolver = baseUserArgumentResolver;
        this.requestLogInterceptor = requestLogInterceptor;
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
