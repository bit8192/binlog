package cn.bincker.web.blog.security.config;

import cn.bincker.web.blog.base.config.SystemProfile;
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
    private final SystemProfile systemProfile;

    public SpringSecurityConfig(
            AuthenticationHandler authenticationHandler,
            VerifyCodeFilter verifyCodeFilter,
            SecurityExceptionHandingConfigurer securityExceptionHandingConfigurer,
            UserDetailsService userDetailsService,
            ApplicationContext applicationContext,
            @Value("${binlog.remember-me-key:d#>bc49c&8475$41d6*ab0a.8ca863588e63}") String rememberMeKey,
            SystemProfile systemProfile) {
        this.authenticationHandler = authenticationHandler;
        this.verifyCodeFilter = verifyCodeFilter;
        this.securityExceptionHandingConfigurer = securityExceptionHandingConfigurer;
        this.userDetailsService = userDetailsService;
        this.applicationContext = applicationContext;
        this.rememberMeKey = rememberMeKey;
        this.systemProfile = systemProfile;
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final var blogger = Role.RoleEnum.BLOGGER.toString();
        final var admin = Role.RoleEnum.ADMIN.toString();
        final var apiPath = systemProfile.getApiPath();
        http.authorizeRequests()
                .antMatchers(
                        HttpMethod.POST,
//                        文章点击量
                        apiPath + "/article/*/view",
//                        上传文件
                        apiPath + "/files",
//                        注册用户
                        apiPath + "/users"
                ).permitAll()
                .antMatchers(HttpMethod.POST, "**").authenticated()
                .antMatchers(HttpMethod.PUT, "**").authenticated()
                .antMatchers(HttpMethod.PATCH, "**").authenticated()
                .antMatchers(HttpMethod.DELETE, "**").authenticated()
                .antMatchers(HttpMethod.POST, apiPath + "/article-classes", apiPath + "/article", apiPath + "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.PUT, apiPath + "/article-classes", apiPath + "/article", apiPath + "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.PATCH, apiPath + "/article-classes", apiPath + "/article", apiPath + "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.DELETE, apiPath + "/article").hasRole(blogger)
                .antMatchers(HttpMethod.DELETE, apiPath + "/article-classes", apiPath + "/tags").hasRole(admin)
                .antMatchers(HttpMethod.GET, apiPath + "/users/all").hasAnyRole(admin, blogger)
                .anyRequest().permitAll()

                .and().formLogin()
                .loginProcessingUrl(apiPath + "/authorize")
                .successHandler(authenticationHandler)
                .failureHandler(authenticationHandler)

                .and().logout()
                .logoutUrl(apiPath + "/logout")
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
