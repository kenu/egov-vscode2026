package com.example.springbatchegov.batch;

import com.example.springbatchegov.domain.User;
import com.example.springbatchegov.domain.UserCsvDto;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserItemProcessor implements ItemProcessor<UserCsvDto, User> {

    @Override
    public User process(UserCsvDto dto) {
        // 이메일이 없으면 null 반환 → Spring Batch가 자동으로 skip
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            System.out.println("[SKIP] 이메일 없음: " + dto.getName());
            return null;
        }
        return new User(
                dto.getName(),
                dto.getEmail().toLowerCase(),
                dto.getDepartment(),
                LocalDateTime.now()
        );
    }
}
