package com.example.circular.bad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// [bad] AService ↔ BService 생성자 순환 참조 → 앱 시작 시 BeanCurrentlyInCreationException 발생
@SpringBootApplication
public class BadApp {

    public static void main(String[] args) {
        SpringApplication.run(BadApp.class, args);
    }
}
