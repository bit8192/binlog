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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

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
        log.setRequestUri(request.getRequestURI());
        log.setSessionId(request.getSession(true).getId());
        log.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        var userOptional = userAuditingListener.getCurrentAuditor();
        userOptional.ifPresent(user -> log.setUserId(user.getId()));
        requestLogRepository.save(log);
        return true;
    }
}
