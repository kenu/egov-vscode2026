package com.example.multimodule.notification.internal;

import com.example.multimodule.common.event.OrderCompletedEvent;
import com.example.multimodule.notification.api.NotificationApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationAdapter implements NotificationApi {

    @Override
    public void notify(String targetUserId) {

        // 알림 전송 비즈니스 로직 실행 ...

        log.info("알림 받는사람 = {}", targetUserId);
    }

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {

        // 알림 전송 비즈니스 로직 실행 ...

        log.info("알림 받는사람 = {}", event.userId());
    }
}
