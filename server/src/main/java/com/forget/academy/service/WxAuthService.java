package com.forget.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.common.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class WxAuthService {
    private static final Logger log = LoggerFactory.getLogger(WxAuthService.class);
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final Object tokenLock = new Object();
    private String cachedToken;
    private long tokenExpireAt;

    @Value("${app.wx-appid}")
    private String appid;

    @Value("${app.wx-secret}")
    private String secret;

    public WxAuthService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public WxSession resolveOpenid(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("缺少微信登录 code");
        }
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new BizException("未配置微信小程序 AppID / AppSecret，无法登录");
        }
        try {
            String queryNoSecret = "appid=" + encode(appid)
                    + "&js_code=" + encode(code)
                    + "&grant_type=authorization_code";
            String query = queryNoSecret + "&secret=" + encode(secret);
            JsonNode node = getJson(
                    "http://api.weixin.qq.com/sns/jscode2session?" + queryNoSecret,
                    "http://api.weixin.qq.com/sns/jscode2session?" + query,
                    "https://api.weixin.qq.com/sns/jscode2session?" + query
            );
            if (node.hasNonNull("errcode") && node.get("errcode").asInt() != 0) {
                throw new BizException("微信登录失败：" + node.path("errmsg").asText());
            }
            String openid = node.path("openid").asText();
            if (openid.isBlank()) {
                throw new BizException("微信登录失败，未返回 openid");
            }
            String unionid = node.path("unionid").asText(null);
            return new WxSession(openid, unionid == null || unionid.isBlank() ? null : unionid);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("jscode2session failed: {}", e.toString());
            throw new BizException("微信登录服务异常");
        }
    }

    public String getAccessToken() {
        synchronized (tokenLock) {
            if (cachedToken != null && System.currentTimeMillis() < tokenExpireAt) {
                return cachedToken;
            }
        }
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new BizException("未配置微信小程序 AppID / AppSecret");
        }
        try {
            String query = "grant_type=client_credential&appid=" + encode(appid) + "&secret=" + encode(secret);
            JsonNode node = getJson(
                    "http://api.weixin.qq.com/cgi-bin/token?" + query,
                    "https://api.weixin.qq.com/cgi-bin/token?" + query
            );
            if (node.hasNonNull("errcode") && node.get("errcode").asInt() != 0) {
                throw new BizException("获取微信 access_token 失败：" + node.path("errmsg").asText());
            }
            String token = node.path("access_token").asText();
            int expires = node.path("expires_in").asInt(7200);
            if (token.isBlank()) {
                throw new BizException("获取微信 access_token 失败");
            }
            synchronized (tokenLock) {
                cachedToken = token;
                tokenExpireAt = System.currentTimeMillis() + Math.max(expires - 200, 60) * 1000L;
            }
            return token;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("access_token failed: {}", e.toString());
            throw new BizException("获取微信 access_token 异常");
        }
    }

    private JsonNode getJson(String... urls) throws Exception {
        Exception last = null;
        for (String url : urls) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofSeconds(8)).build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                return mapper.readTree(response.body());
            } catch (Exception e) {
                last = e;
                log.warn("weixin api miss {}: {}", url.replace(secret, "***"), e.toString());
            }
        }
        throw last == null ? new IllegalStateException("weixin api unreachable") : last;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public void invalidateAccessToken() {
        synchronized (tokenLock) {
            cachedToken = null;
            tokenExpireAt = 0;
        }
    }

    public String postJson(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String postJsonFirstOk(String body, String... urls) throws Exception {
        Exception last = null;
        for (String url : urls) {
            try {
                return postJson(url, body);
            } catch (Exception e) {
                last = e;
                log.warn("weixin api miss {}: {}", redact(url), e.toString());
            }
        }
        throw last == null ? new IllegalStateException("weixin api unreachable") : last;
    }

    private String redact(String url) {
        String value = url == null ? "" : url;
        if (secret != null && !secret.isBlank()) {
            value = value.replace(secret, "***");
        }
        return value.replaceAll("access_token=[^&]+", "access_token=***");
    }

    public record WxSession(String openid, String unionid) {}
}
