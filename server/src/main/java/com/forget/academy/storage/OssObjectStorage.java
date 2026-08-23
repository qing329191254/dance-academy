package com.forget.academy.storage;

import com.forget.academy.common.BizException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@Component
public class OssObjectStorage implements ObjectStorage {
    @Override
    public boolean supports(String type) {
        return type != null && Set.of("oss", "aliyun", "aliyun-oss").contains(type.toLowerCase());
    }

    @Override
    public Map<String, String> saveImage(MultipartFile file, String objectKey) {
        throw new BizException("阿里云 OSS 尚未接入。当前请用 STORAGE_TYPE=wx 或 local");
    }
}
