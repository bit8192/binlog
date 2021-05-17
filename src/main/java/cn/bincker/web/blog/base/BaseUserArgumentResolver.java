package cn.bincker.web.blog.base;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * 支持optional, 支持@NotNull
 * 当注释NotNull时，且用户为登录则直接抛出为登录异常
 */
@Component
public class BaseUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserAuditingListener userAuditingListener;

    public BaseUserArgumentResolver(UserAuditingListener userAuditingListener) {
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        var type = methodParameter.getParameterType();
        if(type.equals(BaseUser.class)){
            return true;
        }else if(type.equals(Optional.class)){
            var parameterizedType = (ParameterizedType) methodParameter.getParameter().getParameterizedType();
            return parameterizedType.getActualTypeArguments()[0].equals(BaseUser.class);
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        var result = userAuditingListener.getCurrentAuditor();
        var type = methodParameter.getParameterType();
        if(type.equals(BaseUser.class)){
            if(methodParameter.hasParameterAnnotation(NonNull.class) && result.isEmpty()) throw new UnauthorizedException();
            return result.orElse(null);
        }else{
            return result;
        }
    }
}
