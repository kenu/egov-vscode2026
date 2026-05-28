package com.example.huskydemo.member;

import org.springframework.stereotype.Service;

@Service
public class MemberValidationService {

    public void validateName(String name) {
        if (name == null || name.length() < 2) {
            throw new IllegalArgumentException("이름은 2자 이상이어야 합니다.");
        }
    }
}
