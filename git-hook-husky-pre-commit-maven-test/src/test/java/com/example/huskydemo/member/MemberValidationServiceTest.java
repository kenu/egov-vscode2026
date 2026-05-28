package com.example.huskydemo.member;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MemberValidationServiceTest {

    private final MemberValidationService memberValidationService = new MemberValidationService();

    @Test
    void 이름이_2자_미만이면_예외가_발생한다() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> memberValidationService.validateName("A")
        );

        assertEquals("이름은 2자 이상이어야 합니다.", ex.getMessage());
    }

    @Test
    void 이름이_2자_이상이면_정상처리된다() {
        assertDoesNotThrow(() -> memberValidationService.validateName("홍길동"));
    }
}
