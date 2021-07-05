package cn.bincker.web.blog.base.entity;

import lombok.Data;

@Data
public class QQAccessToken {
    private String accessToken;
    private long expiresIn;
    private String refreshToken;
    private long createdTime = System.currentTimeMillis();
}
