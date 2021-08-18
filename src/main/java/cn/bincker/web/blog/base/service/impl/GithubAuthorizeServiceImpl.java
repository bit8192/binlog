package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.config.GithubAuthorizeConfigProperties;
import cn.bincker.web.blog.base.entity.GithubAccessToken;
import cn.bincker.web.blog.base.entity.GithubUserInfo;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.service.IGithubAuthorizeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConditionalOnProperty(value = "binlog.oauth2.github.use", havingValue = "true")
@Service
public class GithubAuthorizeServiceImpl implements IGithubAuthorizeService {
    private static final String URL_AUTHORIZE = "https://github.com/login/oauth/authorize?client_id=%s&redirect_url=%s&scope=%s&state=%s&allow_signup=%s";
    private static final String URL_GET_ACCESS_TOKEN = "https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s";
    private static final String URL_GET_USER_INFO = "https://api.github.com/user";
    private static final Logger log = LoggerFactory.getLogger(GithubAuthorizeServiceImpl.class);

    private final GithubAuthorizeConfigProperties githubAuthorizeConfigProperties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GithubAuthorizeServiceImpl(GithubAuthorizeConfigProperties githubAuthorizeConfigProperties, ObjectMapper objectMapper) {
        this.githubAuthorizeConfigProperties = githubAuthorizeConfigProperties;
        this.objectMapper = objectMapper;
        var httpClientBuilder = new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(10));
        if(githubAuthorizeConfigProperties.isEnableProxy()){
            httpClientBuilder.proxy(new Proxy(
                    githubAuthorizeConfigProperties.getProxyType(),
                    new InetSocketAddress(
                            githubAuthorizeConfigProperties.getProxyHost(),
                            githubAuthorizeConfigProperties.getProxyPort())
                    )
            );
        }
        httpClient = httpClientBuilder.build();
    }

    @Override
    public String getAuthorizeUrl(String redirectUrl, String state) {
        return String.format(URL_AUTHORIZE, githubAuthorizeConfigProperties.getClientId(), URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8), "read:user", state, "true");
    }

    @Override
    public GithubAccessToken getAccessToken(HttpServletRequest req, String code) {
        var request = new Request.Builder().url(String.format(
                        URL_GET_ACCESS_TOKEN,
                        githubAuthorizeConfigProperties.getClientId(),
                        githubAuthorizeConfigProperties.getClientSecret(),
                        code
                ))
                .method(HttpMethod.POST.name(), RequestBody.create(null, ""))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        try {
            var response = httpClient.newCall(request).execute();
            var body = response.body();
            if(response.code() != HttpStatus.OK.value() || body == null) {
                log.error("获取github access token失败：code=" + response.code() + "\tbody=" + (response.body() == null ? "" : response.body().string()));
                return null;
            }
            return objectMapper.readValue(body.string(), GithubAccessToken.class);
        } catch (IOException  e) {
            log.error("获取github access token 失败", e);
            throw new SystemException(e);
        }
    }

    @Override
    public GithubUserInfo getUserInfo(String accessToken) {
        var request = new Request.Builder().get().url(URL_GET_USER_INFO)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                .build();
        try {
            var response = httpClient.newCall(request).execute();
            var body = response.body();
            if(response.code() != HttpStatus.OK.value() || body == null) {
                log.error("获取github用户信息失败: code=" + response.code() + "\tbody=" + (body == null ? "" : body.string()));
                return null;
            }
            return objectMapper.readValue(body.string(), GithubUserInfo.class);
        } catch (IOException e) {
            log.error("获取github用户信息失败", e);
            throw new SystemException(e);
        }
    }
}
