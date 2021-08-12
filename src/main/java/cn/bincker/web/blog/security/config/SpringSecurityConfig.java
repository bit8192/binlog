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
    private final String basePath;
    private final SecurityExceptionHandingConfigurer securityExceptionHandingConfigurer;
    private final UserDetailsService userDetailsService;
    private final ApplicationContext applicationContext;

    public SpringSecurityConfig(AuthenticationHandler authenticationHandler, VerifyCodeFilter verifyCodeFilter, @Value("${binlog.base-path}") String basePath, SecurityExceptionHandingConfigurer securityExceptionHandingConfigurer, UserDetailsService userDetailsService, ApplicationContext applicationContext) {
        this.authenticationHandler = authenticationHandler;
        this.verifyCodeFilter = verifyCodeFilter;
        this.basePath = basePath;
        this.securityExceptionHandingConfigurer = securityExceptionHandingConfigurer;
        this.userDetailsService = userDetailsService;
        this.applicationContext = applicationContext;
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
                .antMatchers(HttpMethod.POST, basePath + "/article/*/view").permitAll()
                .antMatchers(HttpMethod.POST, "**").authenticated()
                .antMatchers(HttpMethod.PUT, "**").authenticated()
                .antMatchers(HttpMethod.PATCH, "**").authenticated()
                .antMatchers(HttpMethod.DELETE, "**").authenticated()
                .antMatchers(HttpMethod.POST, basePath + "/article-classes", basePath + "/article", basePath + "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.PUT, basePath + "/article-classes", basePath + "/article", basePath + "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.PATCH, basePath + "/article-classes", basePath + "/article", basePath + "/tags").hasAnyRole(admin, blogger)
                .antMatchers(HttpMethod.DELETE, basePath + "/article").hasRole(blogger)
                .antMatchers(HttpMethod.DELETE, basePath + "/article-classes", basePath + "/tags").hasRole(admin)
                .antMatchers(HttpMethod.GET, basePath + "/users/all").hasAnyRole(admin, blogger)
                .anyRequest().permitAll()

                .and().formLogin()
                .loginProcessingUrl(basePath + "/authorize")
                .successHandler(authenticationHandler)
                .failureHandler(authenticationHandler)

                .and().logout()
                .logoutUrl(basePath + "/logout")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler(applicationContext))

                .and().rememberMe()
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
                .key("d#>bc49c&8475$41d6*ab0a.8ca863588e63")

                .and().exceptionHandling(securityExceptionHandingConfigurer)
                .httpBasic(Customizer.withDefaults())
                .cors().disable()
                .csrf().disable()
                .addFilterAt(verifyCodeFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }
}
