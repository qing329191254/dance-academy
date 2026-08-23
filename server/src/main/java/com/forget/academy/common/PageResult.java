package com.forget.academy.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(List<T> list, long total, int page, int size) {
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), page.getNumber() + 1, page.getSize());
    }
}
