# Async Messaging Console Demo

`about-async-messaging.md` 발표 내용을 콘솔에서 설명하기 위한 Spring Boot 예제입니다.

외부 메시지 브로커(Kafka, RabbitMQ 등)를 설치하지 않고, Spring의 `ApplicationEventPublisher`와 비동기 `ApplicationEventMulticaster`로 메시징 구조를 흉내 냅니다.

## 실행

```bash
cd async-messaging
mvn spring-boot:run
```

## 발표에서 보여주는 내용

- 빨간색 `[집중]` 로그: 발표 중 꼭 봐야 하는 핵심 장면
- 동기 처리: 주문 요청 안에서 결제, 재고, 알림, 통계를 모두 처리
- 비동기 처리: 주문 생성 후 `OrderCreatedEvent`만 발행하고 후속 작업은 소비자가 처리
- 결합도 감소: 주문 서비스는 소비자 구현을 직접 호출하지 않음
- 장애 격리: 알림 실패가 주문 생성 실패로 전파되지 않음
- 재시도와 DLQ: 계속 실패하는 메시지를 Dead Letter Queue에 보관
- 멱등성: 같은 메시지가 두 번 와도 이미 처리한 소비자는 건너뜀
- 최종적 일관성: 응답 직후에는 일부 작업이 아직 처리 중일 수 있음

## 핵심 클래스

- `AsyncMessagingApplication`: 발표용 시나리오 실행
- `MessagingConfig`: Spring 이벤트를 비동기 스레드에서 처리하도록 설정
- `OrderService`: 주문 생성 후 이벤트 발행
- `*Consumer`: 결제, 재고, 알림, 통계 후속 작업 처리
- `ProcessedMessageStore`: 중복 메시지 방지
- `DeadLetterQueue`: 실패 메시지 보관
