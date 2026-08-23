package com.forget.academy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaPatcher implements ApplicationRunner {
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE app_user MODIFY COLUMN avatar VARCHAR(2048) NULL");
            statement.execute("ALTER TABLE app_user MODIFY COLUMN birthday VARCHAR(32) NULL");
        } catch (Exception ignored) {
            // 表尚未创建或已是目标结构时忽略
        }
    }
}
