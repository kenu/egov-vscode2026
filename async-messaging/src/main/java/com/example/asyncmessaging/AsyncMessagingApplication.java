package com.example.asyncmessaging;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AsyncMessagingApplication implements ApplicationRunner {

    private final OrderService orderService;
    private final SynchronousOrderFlow synchronousOrderFlow;
    private final DeadLetterQueue deadLetterQueue;
    private final ConfigurableApplicationContext applicationContext;

    public AsyncMessagingApplication(
            OrderService orderService,
            SynchronousOrderFlow synchronousOrderFlow,
            DeadLetterQueue deadLetterQueue,
            ConfigurableApplicationContext applicationContext
    ) {
        this.orderService = orderService;
        this.synchronousOrderFlow = synchronousOrderFlow;
        this.deadLetterQueue = deadLetterQueue;
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        SpringApplication.run(AsyncMessagingApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        DemoLog.title("비동기 메시징 시스템 도입 조건 - Spring Boot 콘솔 데모");

        DemoLog.section("1. 동기 처리: 모든 후속 작업이 끝날 때까지 요청 스레드가 대기");
        synchronousOrderFlow.placeOrder("SYNC-ORDER-001");

        DemoLog.section("2. 비동기 처리: 주문 생성 후 이벤트만 발행하고 바로 응답");
        orderService.placeOrder("ASYNC-ORDER-001", "keyboard", 1, new BigDecimal("89000"), false, false);

        DemoLog.pause(Duration.ofMillis(900));
        DemoLog.section("3. 중복 전달: 같은 이벤트가 두 번 와도 소비자가 멱등성으로 방어");
        orderService.placeOrder("ASYNC-ORDER-002", "monitor", 1, new BigDecimal("240000"), true, false);

        DemoLog.pause(Duration.ofMillis(900));
        DemoLog.section("4. 장애 격리와 DLQ: 알림 실패가 주문 성공을 되돌리지 않음");
        orderService.placeOrder("ASYNC-ORDER-003", "notebook", 1, new BigDecimal("1290000"), false, true);

        DemoLog.pause(Duration.ofSeconds(5));
        DemoLog.section("5. 운영 관점: DLQ에 남은 실패 메시지 확인");
        deadLetterQueue.printAll();

        DemoLog.section("데모 종료");
        DemoLog.info("발표 연결 문장: 비동기 메시징은 빠르게 처리하는 마법이 아니라, 핵심 흐름과 후속 작업을 안정적으로 분리하는 구조적 선택입니다.");
        applicationContext.close();
    }
}
