# Spring Bean 순환 참조 해결 사례

## 1. 발표 주제

이번 발표에서는 **Spring Bean 순환 참조**가 무엇인지, 왜 문제가 되는지, 그리고 어떻게 해결하는 것이 좋은지 설명합니다.

특히 eGovFrame VSCode Initializr처럼 Spring 기반 코드를 생성하는 프로젝트에서는 사용자가 생성된 코드를 확장하다가 의존 관계를 잘못 설계하면 순환 참조가 발생할 수 있습니다.

그래서 이번 발표의 핵심은 다음과 같습니다.

```text
Spring Bean 순환 참조는 단순한 오류가 아니라
객체 사이의 책임과 의존 방향이 잘못 설계되었다는 신호일 수 있다.
```

---

## 2. 먼저 Bean이 무엇인지부터 이해하기

Spring에서 **Bean**은 Spring 컨테이너가 직접 생성하고 관리하는 객체입니다.

예를 들어 다음과 같은 클래스가 있다고 해보겠습니다.

```java
@Service
public class MemberService {
}
```

여기서 `@Service`가 붙어 있으면 Spring은 이 클래스를 보고 “이 객체는 내가 대신 만들어서 관리해야겠다”라고 판단합니다.

이렇게 Spring이 관리하는 객체를 **Bean**이라고 부릅니다.

쉽게 말하면 다음과 같습니다.

```text
일반 객체:
개발자가 직접 new로 생성하는 객체

Spring Bean:
Spring이 대신 생성하고 관리해주는 객체(제어의 역전)
```

예를 들어 직접 객체를 만들면 다음과 같습니다.

```java
MemberService memberService = new MemberService();
```

하지만 Spring에서는 보통 이렇게 직접 만들지 않습니다.

대신 Spring이 애플리케이션 실행 시점에 객체를 만들고, 필요한 곳에 자동으로 넣어줍니다.

이 과정을 **의존성 주입(Dependency Injection)** 이라고 합니다.

---

## 3. 의존성 주입이란?

의존성 주입은 어떤 객체가 필요한 다른 객체를 직접 만들지 않고, 외부에서 전달받는 방식입니다.

예를 들어 `MemberController`가 `MemberService`를 사용해야 한다고 해보겠습니다.

```java
@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

여기서 `MemberController`는 `MemberService`를 직접 `new`로 만들지 않습니다.

대신 생성자를 통해 `MemberService`를 전달받습니다.

```text
MemberController는 MemberService가 필요하다.
그런데 직접 만들지 않고 Spring에게 받아서 사용한다.
```

Spring은 애플리케이션을 실행할 때 다음과 같이 생각합니다.

```text
1. MemberService Bean을 만든다.
2. MemberController Bean을 만든다.
3. MemberController를 만들 때 MemberService를 넣어준다.
```

이처럼 객체 생성과 연결을 Spring이 대신 처리해주기 때문에 개발자는 비즈니스 로직에 더 집중할 수 있습니다.

---

## 4. 순환 참조란?

순환 참조는 두 개 이상의 객체가 서로를 필요로 하는 구조입니다.

가장 단순한 예시는 다음과 같습니다.

```text
AService → BService
BService → AService
```

즉, AService는 BService가 필요하고, BService도 다시 AService가 필요한 상황입니다.

코드로 보면 다음과 같습니다.

```java
@Service
public class AService {

    private final BService bService;

    public AService(BService bService) {
        this.bService = bService;
    }
}
```

```java
@Service
public class BService {

    private final AService aService;

    public BService(AService aService) {
        this.aService = aService;
    }
}
```

이 구조에서는 Spring이 객체를 만들 때 곤란해집니다.

Spring 입장에서 생각해보면 다음과 같습니다.

```text
AService를 만들려면 BService가 필요하다.
그런데 BService를 만들려면 AService가 필요하다.
그런데 AService를 만들려면 다시 BService가 필요하다.
...
```

결국 끝나지 않는 의존 관계가 만들어집니다.

그래서 이런 구조를 **Spring Bean 순환 참조(Spring Bean Circular Reference)** 라고 합니다.

---

## 5. 순환 참조를 일상적인 비유로 이해하기

순환 참조는 일상생활로 비유하면 더 쉽게 이해할 수 있습니다.

두 사람이 서로에게 이렇게 말하는 상황을 생각해보겠습니다.

```text
A: 나는 B가 와야 출발할 수 있어.
B: 나는 A가 와야 출발할 수 있어.
```

그러면 둘 다 출발하지 못합니다.

A가 먼저 움직이려면 B가 필요하고, B가 먼저 움직이려면 A가 필요하기 때문입니다.

Spring Bean 순환 참조도 비슷합니다.

```text
AService가 생성되려면 BService가 필요하다.
BService가 생성되려면 AService가 필요하다.
```

결과적으로 Spring은 어떤 Bean을 먼저 완성해야 하는지 결정하기 어려워집니다.

---

## 6. 왜 순환 참조가 문제가 될까?

순환 참조가 문제가 되는 이유는 단순히 애플리케이션 실행이 실패할 수 있기 때문만은 아닙니다.

더 중요한 이유는 **설계가 복잡해지고 책임이 섞였다는 신호**일 수 있기 때문입니다.

예를 들어 다음 구조를 보겠습니다.

```text
OrderService → StockService
StockService → OrderService
```

주문 서비스가 재고 서비스를 호출하는 것은 자연스러울 수 있습니다.

```text
주문을 생성하려면 재고를 확인해야 한다.
```

하지만 재고 서비스가 다시 주문 서비스를 호출한다면 구조가 복잡해집니다.

```text
재고를 처리하려면 주문 정보를 다시 확인해야 한다.
```

이런 구조가 반복되면 다음 문제가 생깁니다.

```text
1. 어떤 서비스가 어떤 책임을 가지는지 불분명해진다.
2. 서비스끼리 너무 강하게 묶인다.
3. 테스트하기 어려워진다.
4. 하나를 수정하면 다른 곳까지 영향을 받는다.
5. 애플리케이션 시작 시점에 Bean 생성 오류가 발생할 수 있다.
```

따라서 순환 참조는 단순한 문법 문제가 아니라 **객체 설계와 계층 구조의 문제**로 바라보는 것이 좋습니다.

---

## 7. eGovFrame VSCode Initializr에서의 맥락

eGovFrame VSCode Initializr는 eGovFrame 기반 프로젝트와 CRUD 코드를 생성하는 VS Code 확장입니다.

CRUD 코드 생성에서는 보통 다음과 같은 계층 구조가 만들어집니다.

```text
Controller
    ↓
Service
    ↓
ServiceImpl
    ↓
Mapper
```

각 계층의 역할은 다음과 같습니다.

```text
Controller:
사용자의 요청을 받는 계층

Service:
비즈니스 흐름을 처리하는 계층

ServiceImpl:
Service 인터페이스의 실제 구현체

Mapper:
데이터베이스 접근을 담당하는 계층
```

이 구조에서 중요한 점은 **의존 방향이 한쪽으로 흐른다**는 것입니다.

```text
Controller → Service → Mapper
```

Controller는 Service를 알아도 됩니다. Service는 Mapper를 알아도 됩니다.

하지만 Mapper가 Service를 알거나, Service가 Controller를 알면 구조가 이상해집니다.

---

## 8. 정상적인 계층 구조 예시

정상적인 구조는 다음과 같습니다.

```java
@Controller
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }
}
```

```java
public interface SampleService {
    void createSample();
}
```

```java
@Service
public class SampleServiceImpl implements SampleService {

    private final SampleMapper sampleMapper;

    public SampleServiceImpl(SampleMapper sampleMapper) {
        this.sampleMapper = sampleMapper;
    }

    @Override
    public void createSample() {
        sampleMapper.insertSample();
    }
}
```

```java
@Mapper
public interface SampleMapper {
    void insertSample();
}
```

이 구조의 흐름은 다음과 같습니다.

```text
SampleController
    ↓
SampleService
    ↓
SampleServiceImpl
    ↓
SampleMapper
```

요청을 받는 쪽에서 비즈니스 계층으로, 비즈니스 계층에서 데이터 접근 계층으로 흐릅니다.

이런 구조는 이해하기 쉽고 테스트하기도 좋습니다.

---

## 9. 순환 참조가 생기는 잘못된 예시

문제는 서비스를 확장하다가 다음과 같은 구조가 생길 때입니다.

```text
SampleServiceImpl → AnotherService
AnotherServiceImpl → SampleService
```

코드로 보면 다음과 같습니다.

```java
@Service
public class SampleServiceImpl implements SampleService {

    private final AnotherService anotherService;

    public SampleServiceImpl(AnotherService anotherService) {
        this.anotherService = anotherService;
    }
}
```

```java
@Service
public class AnotherServiceImpl implements AnotherService {

    private final SampleService sampleService;

    public AnotherServiceImpl(SampleService sampleService) {
        this.sampleService = sampleService;
    }
}
```

이 경우 Spring은 다음과 같은 상황에 빠집니다.

```text
SampleServiceImpl을 만들려면 AnotherService가 필요하다.
AnotherServiceImpl을 만들려면 SampleService가 필요하다.
SampleService는 다시 SampleServiceImpl을 의미한다.
결국 다시 처음으로 돌아간다.
```

그 결과 애플리케이션 시작 단계에서 오류가 발생할 수 있습니다.

---

## 10. 임시 해결 방법: @Lazy

순환 참조가 발생했을 때 `@Lazy`를 사용하면 당장 오류를 피할 수 있는 경우가 있습니다.

```java
@Service
public class SampleServiceImpl implements SampleService {

    private final AnotherService anotherService;

    public SampleServiceImpl(@Lazy AnotherService anotherService) {
        this.anotherService = anotherService;
    }
}
```

`@Lazy`는 Bean을 즉시 생성하지 않고, 실제로 필요할 때 늦게 생성하도록 합니다.

그래서 순환 참조 문제를 우회할 수 있습니다.

하지만 이 방식은 근본적인 해결이 아닙니다.

```text
문제의 원인:
서비스끼리 서로 의존하는 구조

@Lazy의 효과:
Bean 생성을 늦춰서 당장 실행되게 함

남아 있는 문제:
서비스 간 책임이 섞인 구조는 그대로 남아 있음
```

즉, `@Lazy`는 임시 처방일 수는 있지만 좋은 설계 개선이라고 보기는 어렵습니다.

---

## 11. 임시 해결 방법: allow-circular-references

또 다른 방법은 설정으로 순환 참조를 허용하는 것입니다.

```properties
spring.main.allow-circular-references=true
```

이 설정을 사용하면 Spring Boot에서 순환 참조를 허용할 수 있습니다.

하지만 이것도 근본적인 해결 방법은 아닙니다.

왜냐하면 잘못된 의존 구조를 그대로 둔 채, Spring에게 “이 구조를 그냥 허용해줘”라고 말하는 것이기 때문입니다.

오픈소스 프로젝트나 코드 생성 도구에서는 이런 방식보다 사용자에게 더 좋은 구조를 안내하는 것이 중요합니다.

따라서 권장 방향은 다음과 같습니다.

```text
설정으로 숨기기보다
의존 구조를 개선한다.
```

---

## 12. 좋은 해결 방법 1: 공통 책임 분리하기

가장 대표적인 해결 방법은 **두 서비스가 함께 필요로 하는 로직을 별도 객체로 분리하는 것**입니다.

문제가 되는 구조는 다음과 같습니다.

```text
AService ↔ BService
```

이 구조를 다음과 같이 바꿀 수 있습니다.

```text
AService → CommonPolicy
BService → CommonPolicy
```

예를 들어 두 서비스가 모두 같은 검증 로직을 필요로 한다고 해보겠습니다.

처음에는 다음처럼 서로를 호출할 수 있습니다.

```text
SampleService는 AnotherService의 검증 기능이 필요하다.
AnotherService는 SampleService의 상태 확인 기능이 필요하다.
```

이 경우 검증과 상태 확인 로직을 별도 컴포넌트로 분리할 수 있습니다.

```java
@Component
public class SamplePolicy {

    public boolean canUpdate(SampleVO sample) {
        // 수정 가능한 상태인지 판단하는 공통 로직
        return true;
    }
}
```

그리고 두 서비스는 서로를 직접 참조하지 않고 `SamplePolicy`를 사용합니다.

```java
@Service
public class SampleServiceImpl implements SampleService {

    private final SamplePolicy samplePolicy;

    public SampleServiceImpl(SamplePolicy samplePolicy) {
        this.samplePolicy = samplePolicy;
    }
}
```

```java
@Service
public class AnotherServiceImpl implements AnotherService {

    private final SamplePolicy samplePolicy;

    public AnotherServiceImpl(SamplePolicy samplePolicy) {
        this.samplePolicy = samplePolicy;
    }
}
```

이제 구조는 다음과 같이 바뀝니다.

```text
SampleServiceImpl
    ↓
SamplePolicy

AnotherServiceImpl
    ↓
SamplePolicy
```

두 서비스가 서로를 직접 알 필요가 없어졌습니다.

이렇게 하면 순환 참조도 사라지고, 공통 로직도 한 곳에서 관리할 수 있습니다.

---

## 13. 좋은 해결 방법 2: Facade로 흐름 조정하기

두 서비스가 서로를 호출하는 이유가 하나의 큰 업무 흐름을 처리하기 위해서라면 **Facade**를 사용할 수 있습니다.

예를 들어 주문 생성 과정을 생각해보겠습니다.

```text
1. 재고를 확인한다.
2. 재고를 차감한다.
3. 주문을 생성한다.
```

이때 OrderService와 StockService가 서로를 호출하면 순환 참조가 생길 수 있습니다.

잘못된 구조:

```text
OrderService → StockService
StockService → OrderService
```

개선된 구조:

```text
OrderFacade
    ↓
OrderService
    ↓
StockService
```

예시 코드는 다음과 같습니다.

```java
@Service
public class OrderFacade {

    private final OrderService orderService;
    private final StockService stockService;

    public OrderFacade(OrderService orderService, StockService stockService) {
        this.orderService = orderService;
        this.stockService = stockService;
    }

    public void createOrder(OrderRequest request) {
        stockService.decreaseStock(request);
        orderService.createOrder(request);
    }
}
```

여기서 중요한 점은 `OrderService`와 `StockService`가 서로를 직접 호출하지 않는다는 것입니다.

업무 흐름은 `OrderFacade`가 조정합니다.

각 서비스는 자신의 책임에 집중합니다.

```text
OrderService:
주문 생성 책임

StockService:
재고 처리 책임

OrderFacade:
주문 생성이라는 전체 흐름 조정
```

이렇게 하면 서비스 간 결합도가 낮아집니다.

---

## 14. 좋은 해결 방법 3: 계층 방향 지키기

Spring 기반 애플리케이션에서는 계층 방향을 지키는 것이 중요합니다.

권장 방향은 다음과 같습니다.

```text
Controller → Service → Mapper
```

피해야 하는 방향은 다음과 같습니다.

```text
Service → Controller
Mapper → Service
ServiceA ↔ ServiceB
```

예를 들어 다음 코드는 좋지 않습니다.

```java
@Service
public class SampleServiceImpl {

    private final SampleController sampleController;

    public SampleServiceImpl(SampleController sampleController) {
        this.sampleController = sampleController;
    }
}
```

왜 좋지 않을까요?

Controller는 사용자의 요청을 받는 계층입니다. Service는 비즈니스 로직을 처리하는 계층입니다.

Service가 Controller를 의존하면 다음과 같은 문제가 생깁니다.

```text
1. 비즈니스 로직이 웹 계층에 의존하게 된다.
2. 테스트하기 어려워진다.
3. 웹 요청이 아닌 다른 방식으로 Service를 재사용하기 어렵다.
4. 계층 구조가 뒤집힌다.
```

따라서 Service는 Controller를 몰라야 합니다.

---

## 15. 해결 방향을 한 문장으로 정리하기

순환 참조를 해결할 때는 다음 문장을 기억하면 좋습니다.

```text
서로를 직접 부르게 하지 말고,
공통 책임을 분리하거나,
상위 흐름 조정 객체를 둔다.
```

즉, 문제 상황은 다음과 같습니다.

```text
AService ↔ BService
```

해결 방향은 두 가지가 대표적입니다.

```text
방법 1:
AService → CommonPolicy
BService → CommonPolicy
```

```text
방법 2:
Facade → AService
Facade → BService
```

첫 번째 방법은 공통 로직이 섞여 있을 때 좋습니다.

두 번째 방법은 여러 서비스를 묶는 업무 흐름이 필요할 때 좋습니다.

---

## 16. 이번 문서에서 제안하는 체크리스트

Spring Bean 순환 참조 문제를 만났을 때 다음 순서로 확인하면 좋습니다.

```text
1. 어떤 Bean들이 서로 참조하고 있는지 확인한다.
2. 두 객체가 정말 서로를 알아야 하는지 확인한다.
3. 공통으로 필요한 로직이 있는지 확인한다.
4. 공통 로직은 Policy, Validator, Resolver 등으로 분리한다.
5. 여러 서비스를 조합하는 흐름이라면 Facade를 둔다.
6. Controller → Service → Mapper 방향이 깨지지 않았는지 확인한다.
7. @Lazy나 allow-circular-references는 마지막 수단으로만 고려한다.
```

이 체크리스트를 따르면 단순히 오류를 없애는 데서 끝나지 않고, 더 유지보수하기 쉬운 구조로 개선할 수 있습니다.

---

## 17. eGovFrame 코드 생성 관점에서의 의미

eGovFrame VSCode Initializr는 사용자가 빠르게 CRUD 코드를 만들 수 있도록 도와주는 도구입니다.

따라서 생성되는 코드나 문서에서 좋은 구조를 안내하는 것이 중요합니다.

사용자는 생성된 코드를 기반으로 기능을 확장합니다.

이때 처음부터 계층 방향과 의존 관계를 명확히 이해하면 나중에 순환 참조나 강한 결합 문제를 줄일 수 있습니다.

이번 문서의 의미는 다음과 같습니다.

```text
단순히 오류 해결 방법을 알려주는 것이 아니라,
Spring 기반 코드에서 의존 방향을 어떻게 설계해야 하는지 안내한다.
```

즉, 이 문서는 eGovFrame 사용자와 기여자가 더 안정적인 Spring 구조를 이해하는 데 도움을 줄 수 있습니다.

