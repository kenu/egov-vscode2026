package com.example.asyncmessaging;

import java.time.Duration;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class NotificationConsumer {

    private static final String NAME = "NotificationConsumer";
    private static final int MAX_ATTEMPTS = 3;

    private final ProcessedMessageStore processedMessageStore;
    private final DeadLetterQueue deadLetterQueue;

    NotificationConsumer(ProcessedMessageStore processedMessageStore, DeadLetterQueue deadLetterQueue) {
        this.processedMessageStore = processedMessageStore;
        this.deadLetterQueue = deadLetterQueue;
    }

    @EventListener
    void on(OrderCreatedEvent event) {
        if (!processedMessageStore.markIfFirst(NAME, event.eventId())) {
            DemoLog.focus(NAME + " 중복 메시지 스킵: 같은 메시지가 두 번 와도 한 번만 처리합니다. eventId=" + event.eventId());
            return;
        }

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            DemoLog.sleep(NAME + " 알림 발송 시도 " + attempt + "/" + MAX_ATTEMPTS + ": orderId=" + event.orderId(), Duration.ofMillis(300));

            if (!event.notificationAlwaysFails() && attempt == 2) {
                DemoLog.info(NAME + " 재시도 후 처리 완료");
                return;
            }

            DemoLog.focus(NAME + " 일시 실패: 외부 알림 서버 응답 없음. 주문 생성은 이미 성공했고, 알림만 다시 시도합니다.");
        }

        deadLetterQueue.add(NAME, event, "알림 발송 " + MAX_ATTEMPTS + "회 실패");
    }
}
