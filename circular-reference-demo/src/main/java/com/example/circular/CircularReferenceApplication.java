package com.example.circular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 시나리오별 실행 방법:
 *
 * 1. [bad]  생성자 순환 참조 → 앱 시작 실패
 *    ./mvnw exec:java -Dexec.mainClass=com.example.circular.bad.BadApp
 *
 * 2. [lazy] @Lazy로 우회 → 시작은 되지만 설계 문제 잔존
 *    ./mvnw exec:java -Dexec.mainClass=com.example.circular.lazy.LazyApp
 *
 * 3. [good] CommonPolicy 분리 → 순환 참조 자체가 없음
 *    ./mvnw exec:java -Dexec.mainClass=com.example.circular.good.GoodApp
 */
@SpringBootApplication(scanBasePackages = "com.example.circular.good")
public class CircularReferenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CircularReferenceApplication.class, args);
    }
}
