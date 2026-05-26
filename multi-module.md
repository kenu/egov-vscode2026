---
marp: true
theme: gaia
paginate: true
---

# Multi-Module Project 설계
## 공통 모듈과 서비스 모듈 분리 기법

### Spring Boot 기반 멀티 모듈 구조로 관심사 분리하기

---

## 1. 왜 멀티 모듈인가?

멀티 모듈이란 하나의 루트(상위) 프로젝트 아래에 여러 개의 독립적인 하위 모듈을 두는 프로젝트 구성 방식으로, 각 모듈은 독립적으로 빌드되며 서로 의존(참조) 할 수 있습니다.

하나의 루트 프로젝트 아래에 여러 하위 모듈을 두고,
`pom.xml`로 모듈 간 의존성을 명시하는 구조입니다.

```text
multi-module
├── common
├── order
├── notification
├── point
└── app
```

단일 모듈에서는 패키지만 달라도 서로 쉽게 참조할 수 있습니다.

```text
// 단일 모듈에서는 이게 가능해버림
// 막을 방법이 없음
OrderService → NotificationService (직접 참조)
OrderService → PointService (직접 참조)
```

멀티 모듈에서는 `pom.xml`에 의존성이 없으면
**컴파일 자체가 안됩니다.**

```text
// order/pom.xml에 notification 없으면
// 컴파일 에러 → 경계 강제!
order → notification (X) ← 빌드 실패
```

즉, 멀티 모듈은 단순한 폴더 분리가 아니라
**경계를 빌드 레벨에서 강제하는 구조**입니다.

---

## 2. 현재 프로젝트 구조

```text
multi-module
├── common
│   └── event/OrderCompletedEvent.java
├── order
│   ├── api/OrderApi.java
│   └── internal/OrderService.java
├── point
│   ├── api/PointApi.java
│   └── internal/PointAdapter.java
├── notification
│   ├── api/NotificationApi.java
│   └── internal/NotificationAdapter.java
└── app
    └── MultiModuleApplication.java
```

---

## 3. 분리 기법 1: POM 의존성 분리

멀티 모듈의 핵심은 각 모듈을 만들고,
`pom.xml`에서 **필요한 의존성만 연결**하는 것입니다.

```xml
<!-- multi-module/pom.xml -->
<packaging>pom</packaging>

<modules>
    <module>common</module>
    <module>notification</module>
    <module>point</module>
    <module>app</module>
    <module>order</module>
</modules>
```

```xml
<!-- order/pom.xml -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>common</artifactId>
    <version>${project.version}</version>
</dependency>
```

필요한 모듈만 의존성으로 추가하므로
참조 가능한 범위가 명확해집니다.

---

## 4. 분리 기법 2: common 모듈 분리

여러 모듈이 **진짜 공통으로 사용하는 것만** common에 둡니다.

```java
// common 모듈
public record OrderCompletedEvent(
        String userId,
        int purchaseAmount
) {}
```

**이것만 common에 두세요**

```text
✅ 여러 모듈이 공유하는 이벤트 클래스
✅ 공통 타입, 공통 유틸
```

**이건 common에 두지 마세요**

```text
❌ 특정 도메인의 Port, DTO, DAO
   → common이 비대해지는 God Module 위험
   → common 변경 시 모든 모듈에 영향
```

---

## 5. 분리 기법 3: api/internal 분리

각 서비스 모듈은 외부에 공개할 것과 내부 구현을 분리합니다.

```text
order
├── api
│   └── OrderApi.java       ← 외부에 공개할 계약
└── internal
    └── OrderService.java   ← 내부 구현
```

```text
✅ O  app → order.api.OrderApi
❌ X  app → order.internal.OrderService
```

**장점:**
- 각 모듈이 자신의 API를 직접 관리
- 내부 구현이 바뀌어도 외부에 영향 없음
- common이 특정 도메인 코드로 오염되지 않음

단, 다른 모듈을 의존하게 되면 internal 패키지의 클래스도 접근이 가능해집니다. 이를 더 엄격하게 제어하고 싶다면 아래와 같은 방법을 고려할 수 있습니다.

- Java 9 JPMS: module-info.java의 exports로 공개 패키지를 명시적으로 제한
- ArchUnit: 아키텍처 테스트로 internal 접근 위반을 빌드 시점에 감지
- 팀 컨벤션: 규칙을 문서화하고 코드 리뷰로 관리

---

## 6. 분리 기법 4: Port 방식

각 모듈의 공개 API를 통해 명시적으로 호출하는 방식입니다.

```java
public void orderWithPort(String userId, String productName, int count) {
    // 주문 처리
    int purchaseAmount = count * 1000;
    log.info("주문 완료: {} - {} - {}원", userId, productName, purchaseAmount);

    // 공개 API를 통해 명시적 호출
    notificationApi.notify(userId);
    pointApi.addPoint(userId, purchaseAmount);
}
```

**장점:**
- 주문 메서드 하나만 봐도 전체 흐름 파악 가능
- 디버깅이 쉬움

**단점:**
- order가 notification, point를 직접 알아야 함
- 후처리 기능이 늘어날수록 주문 로직이 복잡해짐

---

## 7. 분리 기법 5: Event 방식

이벤트를 발행하고 각 모듈이 독립적으로 처리하는 방식입니다.

```java
public void orderWithEvent(String userId, String productName, int count) {
    // 주문 처리
    int purchaseAmount = count * 1000;
    log.info("주문 완료: {} - {} - {}원", userId, productName, purchaseAmount);

    // 이벤트 발행까지만 책임
    eventPublisher.publishEvent(
        new OrderCompletedEvent(userId, purchaseAmount)
    );
}

// notification 모듈 - 독립적으로 처리
@EventListener
public void onOrderCompleted(OrderCompletedEvent event) { }

// point 모듈 - 독립적으로 처리
@EventListener
public void onOrderCompleted(OrderCompletedEvent event) { }
```

---

## 8. 주의할 점

```text
✅ common은 진짜 공통인 것만
✅ 모듈 간 순환 의존성 금지
✅ api/internal 경계 반드시 지키기
✅ 모듈 이름은 기술이 아닌 비즈니스 책임 기준으로
```

특히 common은 편리하지만
**남용하면 모든 모듈이 의존하는**
**거대한 공용 쓰레기통이 될 수 있습니다.**

---

## 9. 마무리

**분리 기법 요약:**

| 기법 | 핵심 |
|---|---|
| POM 의존성 분리 | 필요한 모듈만 명시적으로 연결 |
| common 모듈 분리 | 진짜 공통인 것만 |
| api/internal 분리 | 각 모듈이 자신의 API 관리 |
| Port 방식 | 명시적 호출, 흐름 파악 쉬움 |
| Event 방식 | 느슨한 결합, 확장 용이 |

멀티 모듈은 단순한 폴더 정리가 아니라
**변경에 강한 구조를 만드는 설계 방법**이며
향후 **마이크로서비스로 자연스럽게 분리**할 수 있는
기반이 됩니다.

---
