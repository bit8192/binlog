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
    private String userAgent;
    private String requestUri;
    private String sessionId;
    private Long userId;
}
