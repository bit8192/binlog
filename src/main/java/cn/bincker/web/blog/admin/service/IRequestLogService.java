package cn.bincker.web.blog.admin.service;

import cn.bincker.web.blog.base.entity.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface IRequestLogService {
    Page<RequestLog> getPage(String ip, String address, String host, String referer, String userAgent, String method, String requestUri, String sessionId, String clientId, Long userId, Date start, Date end, Pageable pageable);
}
