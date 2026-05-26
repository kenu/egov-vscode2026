package com.example.circular.good;

import org.springframework.stereotype.Component;

// [good] 두 서비스가 공통으로 필요한 로직을 별도 컴포넌트로 분리
@Component
public class CommonPolicy {

    public String greet() {
        return "CommonPolicy";
    }
}
