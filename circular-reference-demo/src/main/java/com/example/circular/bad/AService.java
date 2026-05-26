package com.example.circular.bad;

import org.springframework.stereotype.Service;

// [bad] AService ↔ BService 생성자 순환 참조 - 앱 시작 시 BeanCurrentlyInCreationException 발생
@Service
public class AService {

    private final BService bService;

    public AService(BService bService) {
        this.bService = bService;
    }

    public String hello() {
        return "AService -> " + bService.greet();
    }
}
