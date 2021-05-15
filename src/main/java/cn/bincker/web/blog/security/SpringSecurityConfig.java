package cn.bincker.web.blog.security;

import cn.bincker.web.blog.security.filter.VerifyCodeFilter;
import cn.bincker.web.blog.security.machine.ApiBackgroundGenerator;
import cn.bincker.web.blog.security.machine.ChineseVerifyCode;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthenticationHandler authenticationHandler;
    private final VerifyCodeFilter verifyCodeFilter;

    public SpringSecurityConfig(AuthenticationHandler authenticationHandler, VerifyCodeFilter verifyCodeFilter) {
        this.authenticationHandler = authenticationHandler;
        this.verifyCodeFilter = verifyCodeFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/authentication").permitAll()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginProcessingUrl("/api/authentication")
                .successHandler(authenticationHandler)
                .failureHandler(authenticationHandler)
                .and()
                .httpBasic(Customizer.withDefaults())
                .cors().disable()
                .csrf().disable()
                .addFilterAt(verifyCodeFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }
}
