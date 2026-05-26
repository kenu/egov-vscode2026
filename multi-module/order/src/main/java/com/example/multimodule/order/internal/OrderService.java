package com.example.multimodule.order.internal;

import com.example.multimodule.common.event.OrderCompletedEvent;
import com.example.multimodule.notification.api.NotificationApi;
import com.example.multimodule.order.api.OrderApi;
import com.example.multimodule.point.api.PointApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderApi {

    private final NotificationApi notificationApi;
    private final PointApi pointApi;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void orderWithPort(String userId, String productName, int count) {

        // 주문 비즈니스 로직 처리 ...
        int purchaseAmount = count * 1000;
        log.info("주문 완료: {} - {} - {}원", userId, productName, purchaseAmount);

        // 후 처리 기능 수행 (명시적 코드 수행)
        notificationApi.notify(userId);
        pointApi.addPoint(userId, purchaseAmount);
    }

    @Override
    public void orderWithEvent(String userId, String productName, int count) {

        // 주문 비즈니스 로직 처리 ...
        int purchaseAmount = count * 1000;
        log.info("주문 완료: {} - {} - {}원", userId, productName, purchaseAmount);

        // 후 처리 기능 수행 (주문 완료 이벤트)
        eventPublisher.publishEvent(new OrderCompletedEvent(userId, purchaseAmount));
    }
}
