package com.example.asyncmessaging;

import java.time.Duration;

import org.springframework.stereotype.Component;

@Component
class SynchronousOrderFlow {

    void placeOrder(String orderId) {
        DemoLog.info("주문 요청 시작: orderId=" + orderId);
        DemoLog.sleep("결제 처리", Duration.ofMillis(500));
        DemoLog.sleep("재고 차감", Duration.ofMillis(450));
        DemoLog.sleep("알림 발송", Duration.ofMillis(550));
        DemoLog.sleep("통계 반영", Duration.ofMillis(350));
        DemoLog.focus("사용자 응답 반환: 동기 방식은 모든 후속 작업이 끝난 뒤에야 응답합니다.");
    }
}
