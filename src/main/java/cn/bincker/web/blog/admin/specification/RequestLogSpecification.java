package cn.bincker.web.blog.admin.specification;

import cn.bincker.web.blog.base.entity.RequestLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Date;

public class RequestLogSpecification {
    public static Specification<RequestLog> ipLike(String keywords){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(keywords)) return null;
            return cb.like(root.get("ip"), keywords);
        };
    }

    public static Specification<RequestLog> addressLike(String keywords){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(keywords)) return null;
            return cb.like(root.get("address"), keywords);
        };
    }

    public static Specification<RequestLog> refererLike(String keywords){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(keywords)) return null;
            return cb.like(root.get("referer"), keywords);
        };
    }

    public static Specification<RequestLog> userAgentLike(String keywords){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(keywords)) return null;
            return cb.like(root.get("userAgent"), keywords);
        };
    }

    public static Specification<RequestLog> method(String method){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(method)) return null;
            return cb.equal(root.get("method"), method);
        };
    }

    public static Specification<RequestLog> requestUriLike(String keywords){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(keywords)) return null;
            return cb.like(root.get("requestUri"), keywords);
        };
    }

    public static Specification<RequestLog> sessionId(String sessionId){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(sessionId)) return null;
            return cb.equal(root.get("sessionId"), sessionId);
        };
    }

    public static Specification<RequestLog> clientId(String clientId){
        return (root, query, cb) -> {
            if(!StringUtils.hasText(clientId)) return null;
            return cb.equal(root.get("clientId"), clientId);
        };
    }

    public static Specification<RequestLog> userId(Long id){
        return (root, query, cb) -> {
            if(id == null) return null;
            return cb.equal(root.get("userId"), id);
        };
    }

    public static Specification<RequestLog> hostLike(String keywords) {
        return (root, query, cb) -> {
            if(!StringUtils.hasText(keywords)) return null;
            return cb.like(root.get("host"), keywords);
        };
    }

    public static Specification<RequestLog> createdDateAfter(Date start){
        return (root, query, cb) -> {
            if(start == null) return null;
            return cb.greaterThan(root.get("createdDate"), start);
        };
    }

    public static Specification<RequestLog> createdDateBefore(Date end){
        return (root, query, cb) -> {
            if(end == null) return null;
            return cb.lessThan(root.get("createdDate"), end);
        };
    }
}
