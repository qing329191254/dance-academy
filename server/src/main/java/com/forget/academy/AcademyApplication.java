package com.forget.academy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcademyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcademyApplication.class, args);
    }
}
