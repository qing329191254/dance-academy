package com.forget.academy.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ObjectStorage {
    boolean supports(String type);

    Map<String, String> saveImage(MultipartFile file, String objectKey);
}
