package com.example.circular.good;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// [good] CommonPolicy 분리로 순환 참조 자체를 제거
@SpringBootApplication
public class GoodApp {

    public static void main(String[] args) {
        SpringApplication.run(GoodApp.class, args);
    }

    @Bean
    CommandLineRunner run(AService aService, BService bService) {
        return args -> {
            System.out.println("[good] " + aService.hello());
            System.out.println("[good] " + bService.hello());
        };
    }
}
