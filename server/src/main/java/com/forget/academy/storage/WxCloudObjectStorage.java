package com.forget.academy.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.common.BizException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxCloudObjectStorage implements ObjectStorage {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @Value("${app.cos.bucket:}")
    private String bucket;

    @Value("${app.cos.region:}")
    private String region;

    @Value("${app.cos.public-base-url:}")
    private String publicBaseUrl;

    @Value("${app.wx-cos.auth-url:http://api.weixin.qq.com/_/cos/getauth}")
    private String authUrl;

    @Value("${app.wx-cos.meta-url:http://api.weixin.qq.com/_/cos/metaid/encode}")
    private String metaUrl;

    @Override
    public boolean supports(String type) {
        return type != null && Set.of("wx", "wxcloud", "wechat").contains(type.toLowerCase());
    }

    @Override
    public Map<String, String> saveImage(MultipartFile file, String objectKey) {
        if (bucket.isBlank() || region.isBlank()) {
            throw new BizException("云托管对象存储未配置，请在环境变量填写 COS_BUCKET、COS_REGION");
        }
        CosAuth auth = fetchAuth();
        String metaFileId = encodeMeta(objectKey);
        COSClient client = null;
        try (InputStream in = file.getInputStream()) {
            ClientConfig config = new ClientConfig(new Region(region));
            config.setHttpProtocol(HttpProtocol.https);
            client = new COSClient(new BasicSessionCredentials(auth.secretId, auth.secretKey, auth.token), config);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }
            metadata.addUserMetadata("fileid", metaFileId);
            client.putObject(new PutObjectRequest(bucket, objectKey, in, metadata));
            String original = file.getOriginalFilename();
            return Map.of("url", publicUrl(objectKey), "filename", original == null ? objectKey : original);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("wx cloud storage upload failed: {}", e.toString());
            throw new BizException("上传到微信云托管对象存储失败");
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    private CosAuth fetchAuth() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(authUrl))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            String secretId = text(node, "TmpSecretId", "tmpSecretId", "secretId");
            String secretKey = text(node, "TmpSecretKey", "tmpSecretKey", "secretKey");
            String token = text(node, "Token", "token", "SecurityToken");
            if (secretId.isBlank() || secretKey.isBlank() || token.isBlank()) {
                throw new BizException("获取云托管临时密钥失败，请确认服务已部署在微信云托管");
            }
            return new CosAuth(secretId, secretKey, token);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("wx cloud getauth failed: {}", e.toString());
            throw new BizException("当前不在微信云托管环境，无法使用云托管对象存储。本地请将 STORAGE_TYPE 设为 local");
        }
    }

    private String encodeMeta(String objectKey) {
        try {
            String path = objectKey.startsWith("/") ? objectKey : "/" + objectKey;
            String body = objectMapper.writeValueAsString(Map.of(
                    "openid", "",
                    "bucket", bucket,
                    "paths", List.of(path)
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(metaUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            JsonNode list = node.at("/respdata/x_cos_meta_field_strs");
            if (!list.isArray() || list.isEmpty()) {
                list = node.path("x_cos_meta_field_strs");
            }
            if (list.isArray() && !list.isEmpty()) {
                return list.get(0).asText();
            }
            throw new BizException("获取云托管文件元数据失败");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("wx cloud meta encode failed: {}", e.toString());
            throw new BizException("获取云托管文件元数据失败");
        }
    }

    private String publicUrl(String objectKey) {
        String base = publicBaseUrl.isBlank()
                ? "https://" + bucket + ".cos." + region + ".myqcloud.com"
                : publicBaseUrl.replaceAll("/+$", "");
        return base + "/" + objectKey.replaceAll("^/+", "");
    }

    private static String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.path(key);
            if (!value.isMissingNode() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return "";
    }

    private record CosAuth(String secretId, String secretKey, String token) {}
}
