package cn.bincker.web.blog.base.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class RequestLog extends BaseEntity{
    private String ip;
    private String address;
    private String referer;
    private String host;
    private String userAgent;
    private String method;
    private String requestUri;
    private String sessionId;
    private String clientId;//区别设备，储存在cookies里
    private Long userId;
}
