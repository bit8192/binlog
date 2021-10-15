package cn.bincker.web.blog.security.config;

import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.security.filter.VerifyCodeFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthenticationHandler authenticationHandler;
    private final VerifyCodeFilter verifyCodeFilter;
    private final SecurityExceptionHandingConfigurer securityExceptionHandingConfigurer;
    private final UserDetailsService userDetailsService;
    private final ApplicationContext applicationContext;
    private final String rememberMeKey;

    public SpringSecurityConfig(
            AuthenticationHandler authenticationHandler,
            VerifyCodeFilter verifyCodeFilter,
            SecurityExceptionHandingConfigurer securityExceptionHandingConfigurer,
            UserDetailsService userDetailsService,
            ApplicationContext applicationContext,
            @Value("${binlog.remember-me-key:d#>bc49c&8475$41d6*ab0a.8ca863588e63}") String rememberMeKey
    ) {
        this.authenticationHandler = authenticationHandler;
        this.verifyCodeFilter = verifyCodeFilter;
        this.securityExceptionHandingConfigurer = securityExceptionHandingConfigurer;
        this.userDetailsService = userDetailsService;
        this.applicationContext = applicationContext;
        this.rememberMeKey = rememberMeKey;
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final var blogger = Role.RoleEnum.BLOGGER.toString();
        final var admin = Role.RoleEnum.ADMIN.toString();
        http.authorizeRequests()
                .antMatchers(
                        HttpMethod.POST,
//                        文章点击量
                        "/article/*/view",
//                        上传文件
                        "/files",
//                        注册用户
                        "/users"
                ).permitAll()
                .antMatchers(HttpMethod.POST, "**").authenticated()
                .antMatchers(HttpMethod.PUT, "**").authenticated()
                .antMatchers(HttpMethod.PATCH, "**").authenticated()
                .antMatchers(HttpMethod.DELETE, "**").authenticated()
                .antMatchers(HttpMethod.POST, "/article-classes", "/article", "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.PUT, "/article-classes", "/article", "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.PATCH, "/article-classes", "/article", "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.DELETE, "/article").hasRole(blogger)
                .antMatchers(HttpMethod.DELETE, "/article-classes", "/tags").hasRole(admin)
                .antMatchers(HttpMethod.GET, "/users/all").hasAnyRole(admin, blogger)
                .anyRequest().permitAll()

                .and().formLogin()
                .loginProcessingUrl("/authorize")
                .successHandler(authenticationHandler)
                .failureHandler(authenticationHandler)

                .and().logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler(applicationContext))

                .and().rememberMe()
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
                .key(rememberMeKey)

                .and().exceptionHandling(securityExceptionHandingConfigurer)
                .httpBasic(Customizer.withDefaults())
                .cors().disable()
                .csrf().disable()
                .addFilterAt(verifyCodeFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }
}
