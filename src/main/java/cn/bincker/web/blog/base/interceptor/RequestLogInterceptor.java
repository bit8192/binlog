package cn.bincker.web.blog.base.interceptor;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.entity.RequestLog;
import cn.bincker.web.blog.base.repository.IRequestLogRepository;
import cn.bincker.web.blog.base.service.IIpAddressQueryService;
import cn.bincker.web.blog.utils.RequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    private final IRequestLogRepository requestLogRepository;
    private final IIpAddressQueryService ipAddressQueryService;
    private final UserAuditingListener userAuditingListener;

    public RequestLogInterceptor(IRequestLogRepository requestLogRepository, IIpAddressQueryService ipAddressQueryService, UserAuditingListener userAuditingListener) {
        this.requestLogRepository = requestLogRepository;
        this.ipAddressQueryService = ipAddressQueryService;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(handler.getClass().equals(ResourceHttpRequestHandler.class)) return true;
        var serverName = request.getServerName();
        if(serverName.equalsIgnoreCase("localhost") || serverName.equals("127.0.0.1")) return true;
        var ip = RequestUtils.getRequestIp(request);
        var log = new RequestLog();
        log.setIp(ip);
        if(StringUtils.hasText(ip)){
            var ipAddressOptional = ipAddressQueryService.query(ip);
            if(ipAddressOptional.isPresent()){
                var address = ipAddressOptional.get().getAddress();
                log.setAddress(address);
            }
        }
        log.setMethod(request.getMethod());
        log.setRequestUri(request.getRequestURI());
        log.setSessionId(request.getSession(true).getId());
        var cookieOptional = RequestUtils.getCookie(request, "clientId");
        if(cookieOptional.isPresent()){
            log.setClientId(cookieOptional.get().getValue());
        }else{
            log.setClientId(UUID.randomUUID().toString());
            var cookie = new Cookie("clientId", log.getClientId());
            cookie.setPath("/");
            cookie.setMaxAge(3600*24*365*30);
            response.addCookie(cookie);
        }
        log.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        log.setHost(request.getServerName());
        log.setReferer(request.getHeader(HttpHeaders.REFERER));
        var userOptional = userAuditingListener.getCurrentAuditor();
        userOptional.ifPresent(user -> log.setUserId(user.getId()));
        requestLogRepository.save(log);
        return true;
    }
}
