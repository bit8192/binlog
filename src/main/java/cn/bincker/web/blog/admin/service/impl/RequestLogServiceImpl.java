package cn.bincker.web.blog.admin.service.impl;

import cn.bincker.web.blog.admin.service.IRequestLogService;
import cn.bincker.web.blog.admin.specification.RequestLogSpecification;
import cn.bincker.web.blog.base.entity.RequestLog;
import cn.bincker.web.blog.base.repository.IRequestLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RequestLogServiceImpl implements IRequestLogService {
    private final IRequestLogRepository requestLogRepository;

    public RequestLogServiceImpl(IRequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @Override
    public Page<RequestLog> getPage(String ip, String address, String host, String referer, String userAgent, String method, String requestUri, String sessionId, String clientId, Long userId, Date start, Date end, Pageable pageable) {
        var specification = RequestLogSpecification.ipLike(ip)
                .and(RequestLogSpecification.addressLike(address))
                .and(RequestLogSpecification.hostLike(host))
                .and(RequestLogSpecification.refererLike(referer))
                .and(RequestLogSpecification.userAgentLike(userAgent))
                .and(RequestLogSpecification.method(method))
                .and(RequestLogSpecification.requestUriLike(requestUri))
                .and(RequestLogSpecification.sessionId(sessionId))
                .and(RequestLogSpecification.clientId(clientId))
                .and(RequestLogSpecification.userId(userId))
                .and(RequestLogSpecification.createdDateAfter(start))
                .and(RequestLogSpecification.createdDateBefore(end));
        return requestLogRepository.findAll(specification, pageable);
    }
}
