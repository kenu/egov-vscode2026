# Log Management Strategies in Production Environments

## 1. 무엇을 로그로 남겨야 하는가?

> **핵심 원칙:** 트러블슈팅에 도움이 될 로그를 남긴다. 서비스의 특성에 따라 다르지만, 범용적으로 적용되는 기준이 있다.

### 범용적으로 남기는 로그 8가지

| # | 로그 유형 | 목적 |
|---|-----------|------|
| 1 | **요청/응답 로그** | 디버깅 및 성능 분석 |
| 2 | **오류 및 예외 로그** | 장애 대응 |
| 3 | **사용자 활동 로그** | 감사(Audit) 및 사용 분석 |
| 4 | **시스템 상태 로그** | 운영 모니터링 |
| 5 | **DB 쿼리 로그** | 성능 최적화 |
| 6 | **보안 로그** | 침해 대응 및 방어 |
| 7 | **배치 작업 로그** | 백그라운드 프로세스 모니터링 |
| 8 | **디버깅 로그** | 문제 추적 |

> 법적으로 보관 의무가 있는 로그는 반드시 별도 정책으로 관리한다.

---

## 2. 예외와 로그

### 흔한 오해 ❌
> "Checked Exception은 컴파일 타임에, Unchecked Exception은 런타임에 발생한다."

### 올바른 이해 ✅
| 구분 | 특징 | 상속 관계 |
|------|------|-----------|
| **Checked Exception** | 컴파일러가 예외 처리를 **강제** (try-catch 또는 throws 선언 필수) | `extends Exception` |
| **Unchecked Exception** | 예외 처리를 **강제하지 않음** | `extends RuntimeException` |

```
Throwable (Checked)
├── Error (Unchecked)          ← OutOfMemoryError 등, 복구 불가
└── Exception (Checked)
    ├── RuntimeException (Unchecked)   ← NullPointerException 등
    └── 각종 Checked Exception         ← IOException 등
```

> **로그 전략:** Unchecked Exception은 보통 ERROR 레벨로 기록하고, Checked Exception은 예외 처리 흐름에 따라 WARN 또는 ERROR로 구분한다.

---

## 3. 로그 레벨

```
TRACE → DEBUG → INFO → WARN → ERROR → FATAL
 (낮음)                                (높음)
```

### 각 레벨의 의미와 활용

| 레벨 | 의미 | 운영 활용 예시 |
|------|------|----------------|
| **TRACE** | 코드의 세부 실행 경로 추적 | 개발 환경에서만 활성화 |
| **DEBUG** | 개발 중 상태·흐름 파악 | 개발/스테이징 환경 |
| **INFO** | 정상 운영 상태의 중요 이벤트 | 일단위 리포트 대상 |
| **WARN** | 잠재적 문제 (즉각 영향 없음) | 1분간 10회 이상 시 알람 |
| **ERROR** | 복구 필요한 중요 오류 | 1회 발생 시 즉시 알람 |
| **FATAL** | 시스템 운영 불가 수준의 심각한 오류 | 즉시 비상 대응 |

### 로그 포맷 구성 요소
```
2024-11-29T00:11:26.845+09:00  ERROR  9736  ---  [nio-8080-exec-3]  k.c.s.p.GlobalExceptionHandler : 메시지
       시간                     레벨   PID        스레드 이름            패키지 + 클래스
```

---

## 4. Logback 설정 — SLF4J와의 관계

### SLF4J는 인터페이스, Logback은 구현체

```
애플리케이션 코드 (@Slf4j)
        ↓
      SLF4J  ← 추상화 레이어 (PSA: Portable Service Abstraction)
     ↙  ↓  ↘
Logback  Log4j2  기타 로깅 프레임워크
```

> **PSA 패턴:** `@Transactional`처럼, 구현체를 교체해도 코드 변경 없이 동작한다.

### 개발 환경별 Logback 설정 분리

**— Spring Profile 기반으로 설정 파일 분리**
```
resources/
├── logback-spring.xml          ← Profile에 따라 분기
├── logback-dev.xml      ← 개발: 콘솔 출력, DEBUG 레벨
└── logback-prod.xml     ← 운영: 파일 출력, INFO 레벨 + 로그 수집
```

**- logback-spring.xml 분기 설정 예시**
```xml
<configuration>
    <springProfile name="dev">
        <include resource="logback-dev.xml" />
    </springProfile>

    <springProfile name="prod">
        <include resource="logback-prod.xml" />
    </springProfile>

    <!-- 기본값 fallback -->
    <springProfile name="default">
        <include resource="logback-dev.xml" />
    </springProfile>
</configuration>
```

**실행 시 Profile 지정**
```bash
java -jar app.jar --spring.profiles.active=prod
# 또는
java -jar app.jar -Dspring.profiles.active=prod
```
---

## 5. Logback 설정 파일 작성법

### 기본 구조

```xml
<configuration>
    <!-- 1. Property 정의 (변수) -->
    <property name="LOG_PATH" value="./logs" />
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %5level %pid --- [%15.15thread] %-40.40logger{36} : %msg%n" />
    
    <!-- 2. Appender 정의 (로그를 어디에 출력할지) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    
    <!-- 3. Logger 설정 (어떤 레벨로 기록할지) -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### 패턴 문법 상세

| 패턴 | 의미 | 예시 |
|------|------|------|
| `%d{패턴}` | 날짜/시간 | `%d{yyyy-MM-dd HH:mm:ss.SSS}` |
| `%level` | 로그 레벨 | `INFO`, `ERROR` |
| `%5level` | 5자리로 정렬된 레벨 | ` INFO`, `ERROR` |
| `%pid` | 프로세스 ID | `12345` |
| `%thread` | 스레드 이름 | `http-nio-8080-exec-1` |
| `%15.15thread` | 스레드명 15자 고정 (잘림/패딩) | `nio-8080-exec-1` |
| `%logger{길이}` | 로거 이름 (패키지.클래스) | `c.e.s.MyService` |
| `%-40.40logger{36}` | 로거명 40자 왼쪽정렬, 패키지는 36자로 축약 | `c.example.service.MyService            ` |
| `%msg` | 로그 메시지 | `User login failed` |
| `%n` | 줄바꿈 | |
| `%ex` | 예외 스택 트레이스 | |

---

## 6. Logback Appender 활용법

### Appender란?
> 로그를 **어디에(Where)** 출력할지 결정하는 컴포넌트

### 주요 Appender 종류

| Appender | 용도 | 클래스 |
|----------|------|--------|
| **ConsoleAppender** | 콘솔 출력 (개발 환경) | `ch.qos.logback.core.ConsoleAppender` |
| **FileAppender** | 단일 파일 저장 | `ch.qos.logback.core.FileAppender` |
| **RollingFileAppender** | 파일 분할 저장 (운영 필수) | `ch.qos.logback.core.rolling.RollingFileAppender` |
| **AsyncAppender** | 비동기 로깅 (성능 개선) | `ch.qos.logback.classic.AsyncAppender` |

---

### 1) ConsoleAppender — 개발 환경

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        <charset>UTF-8</charset>
    </encoder>
</appender>
```

**출력 예시:**
```
14:23:45.123 [main] INFO  c.example.service.UserService - User created: user123
```

---

### 2) RollingFileAppender — 운영 환경 (핵심!)

**문제:** 로그 파일이 무한정 커지면 디스크 부족 발생  
**해결:** 일정 크기/시간마다 파일을 분할하고 오래된 로그는 자동 삭제

#### 시간 기반 롤링 (권장)
```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <!-- 현재 로그 파일 -->
    <file>${LOG_PATH}/application.log</file>
    
    <encoder>
        <pattern>${LOG_PATTERN}</pattern>
    </encoder>
    
    <!-- 롤링 정책: 날짜별 분리 -->
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <!-- 아카이브 파일명 패턴 -->
        <fileNamePattern>${LOG_PATH}/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
        
        <!-- 파일 크기 제한 (하나의 파일이 100MB 초과 시 분할) -->
        <timeBasedFileNamingAndTriggeringPolicy 
            class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>100MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
        
        <!-- 보관 기간 (30일 이전 로그 자동 삭제) -->
        <maxHistory>30</maxHistory>
        
        <!-- 전체 로그 용량 제한 (10GB 초과 시 오래된 로그부터 삭제) -->
        <totalSizeCap>10GB</totalSizeCap>
    </rollingPolicy>
</appender>
```

**생성되는 파일 예시:**
```
logs/
├── application.log                    ← 현재 로그
├── application.2024-11-28.0.log.gz   ← 어제 로그 (압축)
├── application.2024-11-28.1.log.gz   ← 어제 로그 (100MB 넘어 분할)
└── application.2024-11-27.0.log.gz
```

**주요 설정 설명:**

| 설정 | 의미 | 권장값 |
|------|------|--------|
| `%d{yyyy-MM-dd}` | 날짜별 파일 분리 | 일별 롤링 |
| `%i` | 같은 날짜 내 인덱스 | 크기 초과 시 자동 증가 |
| `maxFileSize` | 파일 하나의 최대 크기 | 50MB ~ 200MB |
| `maxHistory` | 보관 기간 (일 단위) | 일반 로그: 30일, ERROR: 90일 |
| `totalSizeCap` | 전체 로그 최대 용량 | 디스크 여유의 10% 이하 |
| `.gz` | 압축 확장자 | gz 또는 zip (gz 권장) |

---

#### 크기 기반 롤링
```xml
<rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy">
    <fileNamePattern>${LOG_PATH}/application.%i.log.zip</fileNamePattern>
    <minIndex>1</minIndex>
    <maxIndex>10</maxIndex>
</rollingPolicy>

<triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
    <maxFileSize>50MB</maxFileSize>
</triggeringPolicy>
```

**생성되는 파일:**
```
application.log
application.1.log.zip
application.2.log.zip
...
application.10.log.zip  ← 11번째 롤링 시 1번 파일이 삭제되고 순환
```

---

### 3) AsyncAppender — 성능 최적화

**문제:** 로그 쓰기가 동기식이면 애플리케이션 성능 저하  
**해결:** 별도 스레드에서 비동기로 로그 기록

```xml
<!-- 실제 파일 쓰기 Appender -->
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <!-- 위의 RollingFileAppender 설정 -->
</appender>

<!-- 비동기 래퍼 -->
<appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
    <!-- 참조할 실제 Appender -->
    <appender-ref ref="FILE" />
    
    <!-- 큐 크기 (기본값: 256) -->
    <queueSize>512</queueSize>
    
    <!-- 큐가 80% 차면 TRACE/DEBUG/INFO 로그 버림 (ERROR/WARN만 보장) -->
    <!-- 0으로 설정하면 모든 로그 보장 -->
    <discardingThreshold>0</discardingThreshold>
    
    <!-- 애플리케이션 종료 시 큐의 로그를 기다릴 최대 시간 (ms) -->
    <maxFlushTime>5000</maxFlushTime>
    
    <!-- Caller 정보 포함 여부 (성능 영향 있음, 기본 false) -->
    <includeCallerData>false</includeCallerData>
</appender>

<root level="INFO">
    <appender-ref ref="ASYNC_FILE" />
</root>
```

**AsyncAppender 동작 원리:**
```
애플리케이션 스레드 → [큐에 로그 적재] → (즉시 반환)
                                ↓
                        별도 워커 스레드 → 파일 쓰기
```

**주의사항:**
- `discardingThreshold > 0`: 큐가 찰 때 낮은 레벨 로그를 버림 (성능 우선)
- `discardingThreshold = 0`: 모든 로그 보장 (데이터 무결성 우선)
- 큐가 가득 차면 블로킹됨 → `queueSize`를 충분히 크게 설정




---

## 7. 로그 수집 — ELK Stack 도입 이유와 구조

### 왜 로그 수집이 필요한가?

단일 서버에서는 SSH로 로그 파일에 직접 접근할 수 있다.  
하지만 **여러 서버에 서비스가 분산**되면 문제가 생긴다.

```
❌ 서버별 SSH 접속으로 로그 확인 → 비효율, 누락 위험

✅ 중앙 집중식 로그 수집 → 통합 검색, 알람, 시각화
```

### ELK Stack 아키텍처

```
[서비스 A] → Logback → 파일 ─┐
[서비스 B] → Logback → 파일 ─┼→ Logstash(5044) → Elasticsearch(9200) → Kibana or Grafana
[서비스 C] → Logback → 파일 ─┘
```

| 컴포넌트 | 역할 |
|----------|------|
| **Logback** | 애플리케이션에서 로그 생성 및 파일 저장 |
| **Logstash** | 여러 서버의 로그를 수집·가공하여 전송 (포트 5044) |
| **Elasticsearch** | 로그 데이터 저장 및 고속 검색 (포트 9200) |
| **Kibana** | 로그 시각화 및 대시보드 |

---

## 9. 실무 트러블슈팅 체크리스트

### 로그 전략
- [ ] 트러블슈팅에 필요한 8가지 로그 유형을 식별했는가?
- [ ] 예외 유형(Checked/Unchecked)에 따라 적절한 로그 레벨을 부여했는가?
- [ ] 로그 레벨별 알람 정책을 정의했는가? (ERROR → 즉시 알람, WARN → 임계치 알람)

### Logback 설정
- [ ] 개발/운영 환경별로 다른 Logback 설정을 적용했는가?
- [ ] RollingFileAppender로 로그 파일 자동 분할·삭제 설정했는가?
- [ ] ERROR 로그를 별도 파일로 분리하여 빠른 장애 추적이 가능한가?
- [ ] AsyncAppender로 로깅 성능 영향을 최소화했는가?
- [ ] 운영 환경에서 로그 보관 기간(maxHistory)과 용량(totalSizeCap)을 정의했는가?

### 로그 수집
- [ ] 다중 서버 환경에서 중앙 집중식 로그 수집(ELK) 체계를 갖췄는가?
- [ ] Logstash로 전송하기 위한 JSON 포맷 설정을 했는가?

### 성능 및 디스크 관리
- [ ] 로그 파일이 디스크를 압박하지 않도록 용량 제한을 설정했는가?
- [ ] 비동기 로깅으로 애플리케이션 성능 저하를 방지했는가?
- [ ] 불필요한 DEBUG 로그가 운영 환경에서 비활성화되어 있는가?

---