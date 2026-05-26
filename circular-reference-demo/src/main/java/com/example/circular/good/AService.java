package com.example.circular.good;

import org.springframework.stereotype.Service;

// [good] AService는 BService를 모름 - CommonPolicy만 의존
@Service
public class AService {

    private final CommonPolicy commonPolicy;

    public AService(CommonPolicy commonPolicy) {
        this.commonPolicy = commonPolicy;
    }

    public String hello() {
        return "AService -> " + commonPolicy.greet();
    }
}
