package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 성공 케이스: 모든 테스트 통과
 *
 * 실행: .\mvnw.cmd clean package
 * 결과: compile → test(통과) → package → BUILD SUCCESS → target/app.jar 생성
 */
@DisplayName("[성공 케이스] OrderService 테스트")
class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }

    @Test
    @DisplayName("주문 생성 성공 - 총액 자동 계산")
    void testCreateOrder() {
        Order order = orderService.createOrder("노트북", 2, 1500000);

        assertNotNull(order);
        assertEquals("노트북", order.getProduct());
        assertEquals(2, order.getQuantity());
        assertEquals(3000000, order.getTotalAmount());
    }

    @Test
    @DisplayName("합계 금액 계산 정확")
    void testCalculateTotal() {
        int total = orderService.calculateTotal(3, 10000);
        assertEquals(30000, total);
    }

    @Test
    @DisplayName("5만원 이상이면 할인 적용 가능")
    void testDiscountApplicable() {
        assertTrue(orderService.isDiscountApplicable(50000));
        assertFalse(orderService.isDiscountApplicable(49999));
    }

    @Test
    @DisplayName("10% 할인 적용 계산 정확")
    void testApplyDiscount() {
        assertEquals(90000, orderService.applyDiscount(100000));
        assertEquals(30000, orderService.applyDiscount(30000)); // 5만원 미만 → 할인 없음
    }

    @Test
    @DisplayName("상품명 없으면 예외 발생")
    void testCreateOrderWithoutProduct() {
        assertThrows(IllegalArgumentException.class, () ->
            orderService.createOrder("", 1, 1000)
        );
    }

    @Test
    @DisplayName("수량 0 이하면 예외 발생")
    void testCreateOrderWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            orderService.createOrder("상품", 0, 1000)
        );
    }

    @Test
    @DisplayName("단가 0 이하면 예외 발생")
    void testCreateOrderWithZeroPrice() {
        assertThrows(IllegalArgumentException.class, () ->
            orderService.createOrder("상품", 1, 0)
        );
    }
}
