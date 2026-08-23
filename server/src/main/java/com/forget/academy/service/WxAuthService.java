package com.forget.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class WxAuthService {
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();

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
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                    + "&secret=" + secret
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofSeconds(8)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = mapper.readTree(response.body());
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
            throw new BizException("微信登录服务异常");
        }
    }

    public record WxSession(String openid, String unionid) {}
}
