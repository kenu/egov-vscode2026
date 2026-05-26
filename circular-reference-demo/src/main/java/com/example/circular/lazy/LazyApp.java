package com.example.circular.lazy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// [lazy] @Lazy로 순환 참조 우회 → 시작은 성공하지만 서비스 간 의존 구조는 그대로 남음
@SpringBootApplication
public class LazyApp {

    public static void main(String[] args) {
        SpringApplication.run(LazyApp.class, args);
    }

    @Bean
    CommandLineRunner run(AService aService) {
        return args -> System.out.println("[lazy] " + aService.hello());
    }
}
