package com.example.circular.good;

import org.springframework.stereotype.Service;

// [good] BService도 AService를 모름 - CommonPolicy만 의존
@Service
public class BService {

    private final CommonPolicy commonPolicy;

    public BService(CommonPolicy commonPolicy) {
        this.commonPolicy = commonPolicy;
    }

    public String hello() {
        return "BService -> " + commonPolicy.greet();
    }
}
