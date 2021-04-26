package cn.bincker.web.blog.base;

import cn.bincker.web.blog.base.entity.BaseUser;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class BaseUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserAuditingListener userAuditingListener;

    public BaseUserArgumentResolver(UserAuditingListener userAuditingListener) {
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().equals(BaseUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return userAuditingListener.getCurrentAuditor().orElse(null);
    }
}
