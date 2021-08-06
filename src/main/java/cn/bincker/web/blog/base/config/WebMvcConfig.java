package cn.bincker.web.blog.base.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final BaseUserArgumentResolver baseUserArgumentResolver;

    public WebMvcConfig(BaseUserArgumentResolver baseUserArgumentResolver) {
        this.baseUserArgumentResolver = baseUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(baseUserArgumentResolver);
    }
}
