package cn.bincker.web.blog.base.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class RequestLog extends BaseEntity{
    private String ip;
    private String address;
    private String referer;
    private String host;
    @Column(length = 512)
    private String userAgent;
    private String method;
    private String requestUri;
    private String sessionId;
    private String clientId;//区别设备，储存在cookies里
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RequestLog that = (RequestLog) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return 922824683;
    }
}
