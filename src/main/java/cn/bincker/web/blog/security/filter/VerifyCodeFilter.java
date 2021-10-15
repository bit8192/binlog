package cn.bincker.web.blog.security.filter;

import cn.bincker.web.blog.base.vo.SuccessMsgVo;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class VerifyCodeFilter implements Filter {
    private final IVerifyCode<?> verifyCode;
    private final ObjectMapper objectMapper;
    private final List<RequestMatcher> verifyRequestMatcherList;

    public VerifyCodeFilter(IVerifyCode<?> verifyCode, ObjectMapper objectMapper) {
        this.verifyCode = verifyCode;
        this.objectMapper = objectMapper;
        verifyRequestMatcherList = new ArrayList<>();
//        登录
        verifyRequestMatcherList.add(new AntPathRequestMatcher("/authorize", HttpMethod.POST.name()));
//        注册
        verifyRequestMatcherList.add(new AntPathRequestMatcher("/users", HttpMethod.POST.name()));
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException{
        if(isNeedVerifyRequest(request)){
            if(!verify(request)){
                //删掉上一次的答案
                request.getSession().removeAttribute(IVerifyCode.SESSION_ATTRIBUTE_ANSWER);
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                objectMapper.writeValue(response.getWriter(), new SuccessMsgVo(false, "验证码错误"));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isNeedVerifyRequest(HttpServletRequest request){
        return verifyRequestMatcherList.stream().anyMatch(matcher->matcher.matches(request));
    }

    private boolean verify(HttpServletRequest request){
        return verifyCode.verify(request);
    }

    @Override
    public void destroy() {

    }
}
