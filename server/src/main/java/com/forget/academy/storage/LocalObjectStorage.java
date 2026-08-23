package com.forget.academy.storage;

import com.forget.academy.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Component
public class LocalObjectStorage implements ObjectStorage {
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public boolean supports(String type) {
        return type == null || type.isBlank() || Set.of("local", "disk").contains(type.toLowerCase());
    }

    @Override
    public Map<String, String> saveImage(MultipartFile file, String objectKey) {
        try {
            Path target = Path.of(uploadDir, objectKey).toAbsolutePath().normalize();
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
            String original = file.getOriginalFilename();
            return Map.of(
                    "url", "/uploads/" + objectKey.replace('\\', '/'),
                    "filename", original == null ? objectKey : original
            );
        } catch (IOException e) {
            throw new BizException("上传失败");
        }
    }
}
