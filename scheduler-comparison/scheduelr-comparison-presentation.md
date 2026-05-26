---
title: "전자정부프레임워크 내 Scheduler 관리: Quartz vs Spring Task"
subtitle: "언제 무엇을 쓸 것인가 — 실무 사례 중심"
environment: eGovFrame 4.x + VSCode
---

# 전자정부프레임워크 내 Scheduler 관리

> **Quartz vs Spring Task**

## 1. 스케줄러란 무엇인가?
⏰ **서버의 '알람 시계'** 

- **정의:** 특정 시간에 특정 작업을 자동으로 실행해 주는 도구
- **주요 활용 사례:**
    - 매일 새벽 2시: 전날 매출 데이터 집계
    - 매시간 정각: 휴면 계정 전환 안내 메일 발송
    - 5분마다: 실패한 결제 건 재시도

## 2. Spring Task — 내 손안의 스마트폰 알람

### 단순함, 가벼움, 빠름
Spring 프레임워크에 내장되어 있어 설정이 가장 쉽고 직관적입니다.

- 특징: `spring-context`에 포함되어 별도 의존성 추가 불필요

- 사용법: `@EnableScheduling` 활성화 후, 메서드에 `@Scheduled` 어노테이션 한 줄 추가

```java
@Component
public class MySimpleTask {
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void runJob() {
        System.out.println("간편하게 배치 실행!");
    }
}
```
- 장점: 구현이 매우 빠르며, 서버가 1대인 소규모/사내 인트라넷 환경에 최적화되어 있습니다.

## 3. 🚨 실무 위기 사례: 정산 금액이 정확히 2배?

### Spring Task의 치명적 한계: 클러스터 인지 불가

서비스가 성장하여 운영 서버(WAS)를 2대로 이중화(Scale-out)했습니다. 그리고 다음 날, 대참사가 발생했습니다.

- 상황: 서버 2대에 동일한 Spring Task 코드가 배포됨.

- 증상:

    - 정산 금액이 정확히 × 2배로 부풀려짐

    - SMS 발송 배치 결과, 동일 고객에게 문자 2통 발송

    - DB Unique 제약 조건 충돌 에러 발생

- 원인: `@Scheduled`는 철저히 JVM(단일 서버) 단위로 동작합니다. 공유 개념이 없기 때문에, 서버가 N대면 알람도 N번 울립니다.

## 4. 해결책 1: 임시 처방과 과도기 (ShedLock)

### 처방 A: 환경변수로 특정 노드만 실행 (SPOF 위험)

```java
@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
public void dailySettlement() {
    if (!batchEnabled) return; // 특정 서버만 설정값 true
    settlementService.run();
}
```
- **한계**: 간단하지만, 해당 서버가 죽으면 배치가 아예 돌지 않는 치명적 단점 존재.

### 처방 B: ShedLock (분산 락 적용)

```java
@SchedulerLock(name = "dailySettlement", lockAtMostFor = "30m", lockAtLeastFor = "1m")
@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
public void dailySettlement() { ... }
```
- **장점**: Spring Task의 가벼움을 유지하면서 DB나 Redis를 이용해 중복 실행만 방지.

## 5. 해결책 2: Quartz 스케줄러 도입 (정공법)

### 본격 스케줄링 프레임워크: 똑똑한 공유 스케줄 보드

전자정부프레임워크 공식 가이드(FDL)에서 권장하는 가장 확실한 해결책입니다.

특히 eGovFrame 4.x(Spring Boot 기반)에서는 도입이 매우 간편해졌습니다.
- 설정의 단순화: 과거처럼 복잡한 XML을 나열할 필요 없이, pom.xml에 Starter 한 줄만 넣으면 라이브러리 준비는 끝납니다.

```xml
<!-- pom.xml — Quartz는 starter 한 줄이면 끝 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

- 핵심 원리: DB(`QRTZ_` 테이블)를 이용한 중앙 집중형 작업 관리

- 작동 방식 (클러스터링):

    - 여러 서버가 공통의 DB 테이블을 바라봅니다.

    - 자정이 되면 한 서버가 "내가 할게!" 하고 DB에 도장(Row-Lock)을 찍습니다.

    - 도장을 확인한 다른 서버들은 대기하거나 다음 작업을 준비합니다.

- 가져다주는 안정성:

    - 중복 실행 원천 차단 (정산 2배 안녕!)

    - Failover 보장: 작업 중이던 서버가 죽어도 다른 서버가 인계받아 실행

    - Misfire 처리: 서버 점검 등으로 놓친 배치를 어떻게 처리할지 정책 수립 가능


## 6. ⚙️ Quartz의 진짜 숨겨진 강점 — '동적 스케줄링'

다중 서버 환경이 아니더라도 Quartz를 써야 하는 결정적 이유가 있습니다.

- **Spring Task**: 컴파일 타임에 코드로 Cron이 고정됩니다. 운영 중에 알람 시간을 바꾸려면 코드를 수정하고 서버를 재배포해야 합니다.

- **Quartz**: 런타임 조작이 가능합니다.

```java
// 런타임에 관리자 화면에서 입력받은 시간으로 알람 변경
scheduler.rescheduleJob(
    TriggerKey.triggerKey("settlementTrigger", "DYNAMIC"), newTrigger
);
```

- 인사이트: 즉석에서 배치를 멈추거나(pauseJob), 재개하거나(resumeJob), 당장 실행(triggerJob)해야 하는 관리자 백오피스가 필요하다면 Quartz가 유일한 선택지입니다.

## 📊 최종 정리

### 한눈에 비교

| 항목 | Spring Task | Quartz |
|---|---|---|
| **설정 난이도** |	매우 쉬움(내장) | 복잡함(DB 테이블 세팅 등)
| **다중 서버(Cluster)** | ❌ (개별 동작, 중복 실행 위험) | ✅ (안전한 단일 실행 보장) |
| **운영 중 시간 변경** |	❌ (서버 재기동 필요)	| ✅ (API로 동적 제어 가능)
| **추천 상황** | 사내 인트라넷, 단일 서버, 가벼운 작업 | 대국민 서비스, 이중화 서버, 중요 결제/정산 |

### 의사결정 트리

```
배치가 필요한가?
  │
  ├─ 단일 서버이고 실행 시간이 고정인가?
  │    └─ YES 👉 Spring Task (@Scheduled)
  │
  ├─ 다중 서버지만 단순히 중복 실행만 막으면 되는가?
  │    └─ YES 👉 Spring Task + ShedLock
  │
  └─ 운영 중 시간 변경 / 이력 추적 / 서버 장애 대응(Failover)이 필요한가?
       └─ YES 👉 Quartz + JDBCJobStore
```

---