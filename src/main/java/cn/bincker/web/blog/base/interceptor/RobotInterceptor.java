package cn.bincker.web.blog.base.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RobotInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        if(StringUtils.hasText(userAgent) && !isRobot(userAgent)){
            if(request.getRequestURI().length() > 1){
                response.sendRedirect("/index.html#?redirectPath=" + response.encodeURL(request.getRequestURI()));
            }else{
                response.sendRedirect("/index.html");
            }
        }
        return true;
    }

    private boolean isRobot(String userAgent) {
        return userAgent.contains("TweetmemeBot") ||
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
