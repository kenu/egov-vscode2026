package com.example.asyncmessaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    void placeOrder(
            String orderId,
            String productId,
            int quantity,
            BigDecimal amount,
            boolean publishDuplicate,
            boolean notificationAlwaysFails
    ) {
        DemoLog.info("주문 저장 완료: orderId=" + orderId);

        OrderCreatedEvent event = new OrderCreatedEvent(
                "evt-" + UUID.randomUUID(),
                orderId,
                productId,
                quantity,
                amount,
                notificationAlwaysFails,
                Instant.now()
        );

        DemoLog.focus("OrderCreated 이벤트 발행: 주문 서비스는 후속 작업을 직접 호출하지 않고 메시지만 남깁니다. eventId=" + event.eventId());
        eventPublisher.publishEvent(event);

        if (publishDuplicate) {
            DemoLog.focus("네트워크 재시도 상황 가정: 같은 eventId를 한 번 더 발행합니다.");
            eventPublisher.publishEvent(event);
        }

        DemoLog.focus("사용자 응답 반환: 주문이 접수되었습니다. 후속 작업은 비동기로 처리됩니다.");
    }
}
