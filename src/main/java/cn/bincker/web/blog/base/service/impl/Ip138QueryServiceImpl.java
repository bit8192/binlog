package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.constant.RegexpConstant;
import cn.bincker.web.blog.base.service.IIpAddressQueryService;
import cn.bincker.web.blog.base.service.ISystemCacheService;
import cn.bincker.web.blog.base.vo.IpAddressVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * IP138接口，连续获取三百次后被拒绝，但过一会之后又可以了
 */
@Service
public class Ip138QueryServiceImpl implements IIpAddressQueryService {
    private static final Logger log = LoggerFactory.getLogger(Ip138QueryServiceImpl.class);
    private static final Pattern IP_PATTERN = Pattern.compile("var ip_result = ([^;]+);");
    private static final String CACHE_KEY_IP_ADDRESS = "IP-ADDRESS-";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ISystemCacheService systemCacheService;

    public Ip138QueryServiceImpl(ISystemCacheService systemCacheService) {
        this.systemCacheService = systemCacheService;
        this.httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Optional<IpAddressVo> query(String ip) {
        if(!StringUtils.hasText(ip) || !ip.matches(RegexpConstant.IP_VALUE)) return Optional.empty();
        if(ip.equals("127.0.0.1")) {
            var vo = new IpAddressVo();
            vo.setAddress("本地");
            return Optional.of(vo);
        }
        var addressOptional = systemCacheService.getValue(CACHE_KEY_IP_ADDRESS + ip, IpAddressVo.class);
        if(addressOptional.isPresent()) {
            return addressOptional;
        }
        var request = HttpRequest
                .newBuilder(URI.create("https://www.ip138.com/iplookup.asp?ip=" + ip + "&action=2"))
                .setHeader(HttpHeaders.ACCEPT,"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0")
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("GBK")));
            if(response.statusCode() != 200){
                log.error("解析IP地址失败: ip=" + ip + "\tcode=" + response.statusCode());
                return Optional.empty();
            }
            var matcher = IP_PATTERN.matcher(response.body());
            if(matcher.find()){
                var result = this.objectMapper.readValue(matcher.group(1), IpResult.class);
                var vo = new IpAddressVo();
                vo.setAddress(result.place);
                systemCacheService.put(CACHE_KEY_IP_ADDRESS + ip, vo, Duration.ofDays(1));
                return Optional.of(vo);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.error("解析IP地址失败: ip=" + ip, e);
            return Optional.empty();
        }
        return Optional.empty();
    }

//{"ASN归属地":"北京市海淀区  电信 ", "iP段":"220.181.38.0 - 220.181.38.255", "兼容IPv6地址":"::DCB5:2694", "映射IPv6地址":"::FFFF:DCB5:2694", "ip_c_list":[{"begin":3702859264, "end":3702859519, "ct":"中国", "prov":"北京市", "city":"海淀区", "area":"", "idc":"", "yunyin":"电信", "net":""}], "zg":1}

    @Data
    private static class IpResult{
        @JsonProperty("ASN归属地")
        private String place;
    }
}
