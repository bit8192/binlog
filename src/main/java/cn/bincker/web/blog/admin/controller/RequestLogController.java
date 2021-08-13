package cn.bincker.web.blog.admin.controller;

import cn.bincker.web.blog.admin.service.IRequestLogService;
import cn.bincker.web.blog.base.entity.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("${binlog.base-path}/admin/request-log")
public class RequestLogController {
    private final IRequestLogService requestLogService;

    public RequestLogController(IRequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @GetMapping
    public Page<RequestLog> page(
            String ip,
            String address,
            String host,
            String referer,
            String userAgent,
            String method,
            String requestUri,
            String sessionId,
            String clientId,
            Long userId,
            Boolean excludeMe,
            Date start,
            Date end,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return requestLogService.getPage(ip, address, host, referer, userAgent, method, requestUri, sessionId, clientId, userId, excludeMe, start, end, pageable);
    }
}
