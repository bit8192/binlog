package cn.bincker.web.blog.base.config;

import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@SuppressWarnings("JpaEntityListenerWarningsInspection")
@Component
public class UserAuditingListener implements AuditorAware<BaseUser> {
    @Override
    public Optional<BaseUser> getCurrentAuditor() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext == null) return Optional.empty();
        Authentication authentication = securityContext.getAuthentication();
        if(authentication == null) return Optional.empty();
        Object principal = authentication.getPrincipal();
        if(!(principal instanceof AuthorizationUser)) return Optional.empty();
        return Optional.of(((AuthorizationUser) principal).getBaseUser());
    }
}
