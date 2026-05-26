package com.example.circular.lazy;

import org.springframework.stereotype.Service;

@Service
public class BService {

    private final AService aService;

    public BService(AService aService) {
        this.aService = aService;
    }

    public String greet() {
        return "BService";
    }

    public String hello() {
        return "BService -> " + aService.hello();
    }
}
