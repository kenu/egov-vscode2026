package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실패 케이스: 의도적으로 틀린 기댓값 → 테스트 실패 → package 단계 차단
 *
 * 실행: .\mvnw.cmd clean package -Pfailure-demo
 * 결과: compile → test(실패) → BUILD FAILURE → target/app.jar 생성 안 됨
 *
 * Maven 철학: "테스트가 통과되지 않은 결과물은 배포하지 말자"
 */
@DisplayName("[실패 케이스] OrderService 테스트 - 의도적 실패로 package 차단 시연")
class OrderServiceFailingTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }

    @Test
    @DisplayName("합계 금액 계산 - 의도적으로 잘못된 기댓값")
    void testCalculateTotalFails() {
        int total = orderService.calculateTotal(3, 10000);
        // 실제 결과: 30000  /  의도적으로 틀린 기댓값: 99999
        assertEquals(99999, total,
            "실제값 30000 ≠ 기댓값 99999 → 테스트 실패 → package 단계 차단됨");
    }

    @Test
    @DisplayName("할인 기준 금액 - 의도적으로 잘못된 기댓값")
    void testDiscountApplicableFails() {
        // 실제로는 49999 < 50000 이므로 false  /  의도적으로 true 기대
        assertTrue(orderService.isDiscountApplicable(49999),
            "실제값 false ≠ 기댓값 true → 테스트 실패 → package 단계 차단됨");
    }
}
