package cn.bincker.web.blog.security.filter;

import cn.bincker.web.blog.base.vo.SuccessMsgVo;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class VerifyCodeFilter implements Filter {
    private final IVerifyCode<?> verifyCode;
    private final ObjectMapper objectMapper;
    private final String basePath;

    public VerifyCodeFilter(IVerifyCode<?> verifyCode, ObjectMapper objectMapper, @Value("${system.base-path}") String basePath) {
        this.verifyCode = verifyCode;
        this.objectMapper = objectMapper;
        this.basePath = basePath;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

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
        if(!request.getMethod().equals(RequestMethod.POST.name())) return false;
        return request.getRequestURI().startsWith(basePath + "/authentication");
    }

    private boolean verify(HttpServletRequest request){
        return verifyCode.verify(request);
    }

    @Override
    public void destroy() {

    }
}
