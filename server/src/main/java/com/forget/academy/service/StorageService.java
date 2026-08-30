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
    private static final Set<String> IMAGE_EXT = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg");
    private static final Set<String> FILE_EXT = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".pdf");
    private static final Set<String> MEDIA_EXT = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp",
            ".mp4", ".mov", ".m4v");

    private final List<ObjectStorage> storages;

    @Value("${app.storage:local}")
    private String storageType;

    public StorageService(List<ObjectStorage> storages) {
        this.storages = storages;
    }

    public Map<String, String> saveImageBytes(byte[] bytes, String filename) {
        return saveBytes(bytes, filename, IMAGE_EXT, "仅支持图片文件", 8 * 1024 * 1024);
    }

    public Map<String, String> saveFileBytes(byte[] bytes, String filename) {
        return saveBytes(bytes, filename, FILE_EXT, "请上传图片或 PDF", 8 * 1024 * 1024);
    }

    public Map<String, String> saveMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        if (file.getSize() > 35L * 1024 * 1024) {
            throw new BizException("文件过大，请压缩后上传（建议 35MB 以内）");
        }
        String ext = extension(file.getOriginalFilename());
        if (ext.isEmpty()) {
            throw new BizException("无法识别文件类型");
        }
        if (!MEDIA_EXT.contains(ext)) {
            throw new BizException("仅支持图片或视频（mp4/mov）");
        }
        return put(file, ext);
    }

    private Map<String, String> saveBytes(byte[] bytes, String filename, Set<String> allowed, String invalidMessage, int maxBytes) {
        if (bytes == null || bytes.length == 0) {
            throw new BizException("请选择文件");
        }
        if (bytes.length > maxBytes) {
            throw new BizException("文件过大");
        }
        String name = (filename == null || filename.isBlank()) ? "file.jpg" : filename;
        String ext = extension(name);
        if (ext.isEmpty()) {
            ext = ".jpg";
            name = name + ext;
        }
        if (!allowed.contains(ext)) {
            throw new BizException(invalidMessage);
        }
        return put(new ByteArrayMultipartFile("file", name, contentType(ext), bytes), ext);
    }

    public Map<String, String> saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        String ext = extension(file.getOriginalFilename());
        if (ext.isEmpty()) {
            ext = ".jpg";
        }
        if (!IMAGE_EXT.contains(ext)) {
            throw new BizException("仅支持图片文件");
        }
        return put(file, ext);
    }

    private Map<String, String> put(MultipartFile file, String ext) {
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
            case ".pdf" -> "application/pdf";
            case ".mp4" -> "video/mp4";
            case ".mov", ".m4v" -> "video/quicktime";
            default -> "image/jpeg";
        };
    }
}
