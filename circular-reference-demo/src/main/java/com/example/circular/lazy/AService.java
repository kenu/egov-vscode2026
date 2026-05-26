package com.example.circular.lazy;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

// [lazy] @Lazy로 순환 참조 우회 - 시작 오류는 피하지만 설계 문제는 그대로 남음
@Service
public class AService {

    private final BService bService;

    public AService(@Lazy BService bService) {
        this.bService = bService;
    }

    public String hello() {
        return "AService -> " + bService.greet();
    }
}
