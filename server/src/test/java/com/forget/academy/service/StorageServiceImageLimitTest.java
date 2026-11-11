package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.storage.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageServiceImageLimitTest {

    @Test
    void rejectsImageLargerThan2MB() {
        StorageService service = new StorageService(List.of(new AcceptAllStorage()));
        ReflectionTestUtils.setField(service, "storageType", "local");
        byte[] bytes = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", bytes);

        BizException ex = assertThrows(BizException.class, () -> service.saveImage(file));
        assertTrue(ex.getMessage().contains("2MB"));
    }

    @Test
    void acceptsImageWithin2MB() {
        StorageService service = new StorageService(List.of(new AcceptAllStorage()));
        ReflectionTestUtils.setField(service, "storageType", "local");
        byte[] bytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "ok.jpg", "image/jpeg", bytes);

        Map<String, String> result = service.saveImage(file);
        assertEquals("ok", result.get("url"));
    }

    static class AcceptAllStorage implements ObjectStorage {
        @Override
        public boolean supports(String type) {
            return true;
        }

        @Override
        public Map<String, String> saveImage(org.springframework.web.multipart.MultipartFile file, String objectKey) {
            return Map.of("url", "ok", "filename", objectKey);
        }
    }
}
