package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.config.QQAuthorizeConfigProperties;
import cn.bincker.web.blog.base.entity.QQAccessToken;
import cn.bincker.web.blog.base.entity.QQUserInfo;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.service.IQQAuthorizeService;
import cn.bincker.web.blog.utils.RequestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(value = "binlog.oauth2.qq.use", havingValue = "true")
@Service
public class QQAuthorizeServiceImpl implements IQQAuthorizeService {
    private static final Logger log = LoggerFactory.getLogger(QQAuthorizeServiceImpl.class);
    private static final String URL_AUTHORIZE = "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s";
    private static final String URL_GET_ACCESS_TOKEN = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&fmt=json";
    private static final String URL_GET_OPEN_ID = "https://graph.qq.com/oauth2.0/me?access_token=%s&fmt=json";
    private static final String URL_GET_USER_INFO = "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s";
    private final QQAuthorizeConfigProperties configProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QQAuthorizeServiceImpl(QQAuthorizeConfigProperties configProperties, ObjectMapper objectMapper) {
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String getAuthorizeUrl(String redirectUrl, String state) {
        return String.format(URL_AUTHORIZE, configProperties.getAppId(), URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8), state);
    }

    @Override
    public QQAccessToken getAccessToken(HttpServletRequest req, String code) {
        var request = HttpRequest.newBuilder(URI.create(String.format(
                URL_GET_ACCESS_TOKEN,
                configProperties.getAppId(),
                configProperties.getAppKey(),
                code,
                URLEncoder.encode(RequestUtils.getRequestBaseUrl(req) + "/authorize/qq/notice", StandardCharsets.UTF_8)
        ))).GET().build();
        try {
            var response = httpClient.send(
                    request,
                    responseInfo -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), body-> {
                        if(responseInfo.statusCode() != HttpStatus.OK.value()) return null;
                        try {
                            var result = objectMapper.readValue(body, QQAccessToken.class);
                            if(!StringUtils.hasText(result.getAccessToken())){
                                log.error("获取qq AccessToken 失败: body=" + body);
                                return null;
                            }
                            return result;
                        } catch (JsonProcessingException e) {
                            log.error("获取QQAccessToken时, 响应json转换失败: content=" + body, e);
                            return null;
                        }
                    })
            );
            if(response.body() == null) throw new SystemException();
            return response.body();
        } catch (IOException | InterruptedException e) {
            log.error("获取QQAccessToken失败", e);
            throw new SystemException(e);//懒得处理
        }
    }

    @Override
    public String getOpenId(String accessToken) {
        var request = HttpRequest.newBuilder(URI.create(String.format(URL_GET_OPEN_ID, accessToken))).GET().build();
        try {
            var response = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.mapping(
                    HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                    body->{
                        if(responseInfo.statusCode() != HttpStatus.OK.value()){
                            log.error("获取QQ openId 失败：code=" + responseInfo.statusCode() + "\tbody=" + body);
                            return null;
                        }
                        try {
                            //noinspection unchecked
                            var map = (Map<String, String>) objectMapper.readValue(body, objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
                            var openId = map.get("openid");
                            if(!StringUtils.hasText(openId)){
                                log.error("获取QQ openId失败: body=" + body);
                                return null;
                            }
                            return openId;
                        } catch (JsonProcessingException e) {
                            log.error("获取QQOpenId时，响应json解析失败", e);
                            return null;
                        }
                    }
            ));
            if(response.body() == null) throw new SystemException();
            return response.body();
        } catch (IOException | InterruptedException e) {
            log.error("获取QQOpenId失败", e);
            throw new SystemException(e);
        }
    }

    @Override
    public QQUserInfo getUserInfo(String accessToken) {
        var openId = getOpenId(accessToken);
        var request = HttpRequest.newBuilder(URI.create(String.format(
                URL_GET_USER_INFO,
                accessToken,
                configProperties.getAppId(),
                openId
        ))).build();
        try {
            var response = httpClient.send(
                    request,
                    responseInfo->HttpResponse.BodySubscribers.mapping(
                            HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                            body->{
                                if(responseInfo.statusCode() != HttpStatus.OK.value()) {
                                    log.error("获取QQ用户信息失败: code=" + responseInfo.statusCode() + "\tbody=" + body);
                                    return null;
                                }
                                try {
                                    var userInfo = objectMapper.readValue(body, QQUserInfo.class);
                                    if(userInfo.getRet() == -1){
                                        log.error("获取qq用户信息失败：body=" + body);
                                        return null;
                                    }
                                    return userInfo;
                                } catch (JsonProcessingException e) {
                                    log.error("获取qq用户信息时，响应json解析失败", e);
                                    return null;
                                }
                            }
                    )
            );
            if(response.body() == null) throw new SystemException();
            var userInfo = response.body();
            userInfo.setOpenId(openId);
            return userInfo;
        } catch (IOException | InterruptedException e) {
            log.error("获取qq用户信息失败", e);
            throw new SystemException();
        }
    }
}
