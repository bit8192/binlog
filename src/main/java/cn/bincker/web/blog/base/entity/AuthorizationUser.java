package cn.bincker.web.blog.base.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AuthorizationUser implements UserDetails {
    @Getter
    private final BaseUser baseUser;

    public AuthorizationUser(BaseUser baseUser) {
        this.baseUser = baseUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return baseUser.getRoles();
    }

    @Override
    public String getPassword() {
        return baseUser.getEncodedPasswd();
    }

    @Override
    public String getUsername() {
        return baseUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !baseUser.getLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
