package cn.bincker.web.blog.base.interceptor;

import cn.bincker.web.blog.base.annotation.ApiController;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 爬虫拦截器
 */
@Component
public class RobotInterceptor implements HandlerInterceptor {
    private static final String SINGLE_PAGE_APP_REDIRECT_PATH = "/index.html#?redirectPath=";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler.getClass().equals(ResourceHttpRequestHandler.class)) return true;
        var userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        var redirectPathParam = request.getParameter("redirectPath");
        var uri = request.getRequestURI();
        if(isRobot(userAgent)){
            if(StringUtils.hasText(redirectPathParam)){
                response.sendRedirect(URLDecoder.decode(redirectPathParam, StandardCharsets.UTF_8));
            }
        }else if(!isApiRequest(handler)){
            response.sendRedirect(SINGLE_PAGE_APP_REDIRECT_PATH + response.encodeURL(uri));
        }
        return true;
    }

    private boolean isApiRequest(Object handler){
        if(!handler.getClass().equals(HandlerMethod.class)) return false;
        var handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getBeanType().isAnnotationPresent(ApiController.class);
    }

    private boolean isRobot(String userAgent) {
        return !StringUtils.hasText(userAgent) ||
                userAgent.contains("TweetmemeBot") ||
                userAgent.contains("AhrefsBot") ||
                userAgent.contains("Googlebot") ||
                userAgent.contains("bingbot") ||
                userAgent.contains("SemrushBot") ||
                userAgent.contains("DotBot") ||
                userAgent.contains("coccocbot-web") ||
                userAgent.contains("SeznamBot") ||
                userAgent.contains("YandexBot") ||
                userAgent.contains("MJ12bot") ||
                userAgent.contains("AmazonAdBot") ||
                userAgent.contains("contxbot") ||
                userAgent.contains("Mail.RU_Bot") ||
                userAgent.contains("Go-http-client") ||
                userAgent.contains("coccocbot-image") ||
                userAgent.contains("Qwantify/Bleriot") ||
                userAgent.contains("Exabot") ||
                userAgent.contains("DuckDuckBot")
                ;
    }
}
