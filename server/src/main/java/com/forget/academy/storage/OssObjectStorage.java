package com.forget.academy.storage;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.forget.academy.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class OssObjectStorage implements ObjectStorage {
    @Value("${app.oss.endpoint:}")
    private String endpoint;

    @Value("${app.oss.region:}")
    private String region;

    @Value("${app.oss.bucket:}")
    private String bucket;

    @Value("${app.oss.public-base-url:}")
    private String publicBaseUrl;

    @Value("${app.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${app.oss.access-key-secret:}")
    private String accessKeySecret;

    @Override
    public boolean supports(String type) {
        return type != null && Set.of("oss", "aliyun", "aliyun-oss").contains(type.toLowerCase());
    }

    @Override
    public Map<String, String> saveImage(MultipartFile file, String objectKey) {
        requireConfiguration();
        OSS client = null;
        try (InputStream input = file.getInputStream()) {
            ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
            configuration.setSignatureVersion(SignVersion.V4);
            client = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(new DefaultCredentialProvider(accessKeyId, accessKeySecret))
                    .clientConfiguration(configuration)
                    .region(region)
                    .build();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (file.getContentType() != null && !file.getContentType().isBlank()) {
                metadata.setContentType(file.getContentType());
            }
            client.putObject(new PutObjectRequest(bucket, objectKey, input, metadata));

            String original = file.getOriginalFilename();
            return Map.of(
                    "url", publicUrl(objectKey),
                    "filename", original == null ? objectKey : original
            );
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("aliyun oss upload failed: {}", e.toString());
            throw new BizException("上传到阿里云 OSS 失败，请检查 Bucket、区域和 RAM 用户权限");
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    private void requireConfiguration() {
        if (isBlank(endpoint) || isBlank(region) || isBlank(bucket)
                || isBlank(accessKeyId) || isBlank(accessKeySecret)) {
            throw new BizException("阿里云 OSS 未配置完整，请检查 OSS_ENDPOINT、OSS_REGION、OSS_BUCKET 和 RAM 用户 AccessKey");
        }
    }

    private String publicUrl(String objectKey) {
        String base = isBlank(publicBaseUrl)
                ? "https://" + bucket + ".oss-" + region + ".aliyuncs.com"
                : publicBaseUrl.replaceAll("/+$", "");
        return base + "/" + objectKey.replaceAll("^/+", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
