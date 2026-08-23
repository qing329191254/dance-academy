package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.storage.ByteArrayMultipartFile;
import com.forget.academy.storage.ObjectStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {
    private static final Set<String> ALLOWED_EXT = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg");

    private final List<ObjectStorage> storages;

    @Value("${app.storage:local}")
    private String storageType;

    public StorageService(List<ObjectStorage> storages) {
        this.storages = storages;
    }

    public Map<String, String> saveImageBytes(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new BizException("请选择文件");
        }
        if (bytes.length > 8 * 1024 * 1024) {
            throw new BizException("图片过大");
        }
        String name = (filename == null || filename.isBlank()) ? "avatar.jpg" : filename;
        String ext = extension(name);
        if (ext.isEmpty()) {
            ext = ".jpg";
            name = name + ext;
        }
        String contentType = contentType(ext);
        return saveImage(new ByteArrayMultipartFile("file", name, contentType, bytes));
    }

    public Map<String, String> saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        String ext = extension(file.getOriginalFilename());
        if (ext.isEmpty()) {
            ext = ".jpg";
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持图片文件");
        }
        String objectKey = "academy/" + LocalDate.now() + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
        String type = storageType == null ? "local" : storageType.trim();
        return storages.stream()
                .filter(item -> item.supports(type))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的 STORAGE_TYPE: " + type))
                .saveImage(file, objectKey);
    }

    private static String extension(String original) {
        if (original == null) {
            return "";
        }
        int dot = original.lastIndexOf('.');
        return dot >= 0 ? original.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    private static String contentType(String ext) {
        return switch (ext) {
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
