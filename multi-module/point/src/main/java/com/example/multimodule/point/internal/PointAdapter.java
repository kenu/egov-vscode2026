package com.example.multimodule.point.internal;

import com.example.multimodule.common.event.OrderCompletedEvent;
import com.example.multimodule.point.api.PointApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PointAdapter implements PointApi {

    @Override
    public void addPoint(String userId, int purchaseAmount) {

        // 포인트 적립 비즈니스 로직 실행

        log.info("구매자 = {}, 적립 포인트 = {}", userId, purchaseAmount);
    }

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {

        // 포인트 적립 비즈니스 로직 실행

        log.info("구매자 = {}, 적립 포인트 = {}", event.userId(), event.purchaseAmount());
    }
}
