# DBCP (DB Connection Pool) 정리

> HikariCP & MySQL 예제 + 실제 프로젝트(.NET OleDB) 비교

---

## 1. DBCP 없이는 무슨 문제가?

백엔드 서버와 DB 서버는 **TCP 기반**으로 통신한다.  
TCP는 연결 지향적이므로 데이터를 주고받기 전에 반드시 **커넥션을 맺고 닫는 과정**이 필요하다.

```
API 요청 → TCP 연결(3-way handshake) → 쿼리 실행 → TCP 종료(4-way handshake) → 응답 반환
```

**문제점**: 매 DB 요청마다 커넥션을 새로 맺고 닫으면 handshake 오버헤드가 누적되어 API 응답 시간이 늘어나고 전체 성능이 저하된다.

---

## 2. DBCP 개념과 동작 원리

**Database Connection Pool** — 커넥션을 미리 만들어두고 재사용하는 방식.

### 동작 흐름

| 단계 | 설명 |
|------|------|
| ① 서버 시작 | DB와 커넥션 N개를 미리 생성 → 풀에 보관 |
| ② 요청 수신 | 풀에서 유휴(idle) 커넥션 대여 |
| ③ DB 작업 | 빌린 커넥션으로 쿼리 실행 |
| ④ 작업 완료 | `close()` 호출 → **실제 TCP 종료가 아닌 풀 반납** |

> `close()` = 커넥션 종료가 아니라 **풀에 반납**하는 것. TCP handshake 비용이 제거되어 응답 시간이 단축된다.

---

## 3. MySQL 서버 설정

### `max_connections`

클라이언트와 맺을 수 있는 **최대 커넥션 수**.

- 이 값을 넘으면 신규 연결 자체가 실패한다.
- 공식: `백엔드 서버 수 × DBCP max size < max_connections`
- **여유분 필수** — 테스트 서버, 개발자 클라이언트 등도 연결을 사용하므로 넉넉하게 설정한다.

```sql
SHOW VARIABLES LIKE 'max_connections';
SET GLOBAL max_connections = 100;
```

### `wait_timeout`

유휴 커넥션이 요청 없이 대기하는 **최대 시간**.

- 비정상 종료된 커넥션을 DB가 자동으로 정리하는 메커니즘.
- 설정 시간 동안 요청이 없으면 DB가 연결을 끊는다.
- 새 요청이 오면 타이머가 0으로 리셋된다.
- **HikariCP `maxLifetime`과 연계 필수**: `maxLifetime < wait_timeout` (2~5초 여유)

```sql
SHOW VARIABLES LIKE 'wait_timeout';
SET GLOBAL wait_timeout = 1800;
```

---

## 4. HikariCP 설정

Spring Boot 2.0부터 기본 내장된 커넥션 풀 라이브러리.

### `minimumIdle`

풀에서 유지할 **최소 유휴 커넥션 수**.

- 아이들 커넥션이 이 값보다 적어지면 신규 생성.
- 단, 전체 수가 `maximumPoolSize`를 초과하지 않는 범위에서 동작 → `maximumPoolSize`가 더 높은 우선순위.
- **권장**: `maximumPoolSize`와 동일하게 설정 (고정 풀 운영).

### `maximumPoolSize`

풀이 가질 수 있는 **최대 커넥션 수** (idle + active 합산).

- 트래픽 급증 시 이 수를 초과해 커넥션을 만들지 않는다.
- 대기 요청은 `connectionTimeout`까지 블록된다.
- **권장**: 고정 풀 사이즈로 운영 (성능 예측 가능).

### `maxLifetime`

풀에서 커넥션의 **최대 수명**.

- 수명이 다한 idle 커넥션은 즉시 제거.
- active 커넥션은 반환 후 제거 (**풀로 반환되지 않으면 동작하지 않음!**)
- 제거 후 새 커넥션을 자동 생성해 풀 크기를 유지한다.
- **주의**: `wait_timeout`보다 **2~5초 짧게** 설정해야 한다.

### `connectionTimeout`

풀에서 커넥션을 받기 위해 **대기하는 최대 시간**.

- 이 시간 안에 커넥션을 못 받으면 예외(Exception) 발생.
- 일반 사용자 요청 기준 **10초 이하** 권장.
- 클라이언트가 이미 끊었다면 긴 대기는 의미 없다.

### Spring Boot application.yml 예시

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    hikari:
      # 최대 풀 크기 (idle + active 합산)
      maximum-pool-size: 10
      # 최소 유휴 커넥션 (max와 동일 권장)
      minimum-idle: 10
      # 커넥션 최대 수명 ms (wait_timeout보다 짧게)
      max-lifetime: 1800000
      # 커넥션 획득 대기 최대 시간 ms
      connection-timeout: 30000
```

---

## 5. 핵심 파라미터 관계 정리

| 파라미터 | 위치 | 역할 | 주의사항 |
|----------|------|------|----------|
| `max_connections` | MySQL | DB가 허용하는 전체 최대 커넥션 수 | 서버 수 × DBCP max보다 크게 설정 |
| `wait_timeout` | MySQL | 유휴 커넥션 자동 종료 대기 시간 | `maxLifetime`보다 2~5초 길게 설정 |
| `minimumIdle` | HikariCP | 유지할 최소 유휴 커넥션 수 | `maximumPoolSize`와 동일 권장 |
| `maximumPoolSize` | HikariCP | 풀의 최대 커넥션 수 (idle + active) | 부하 테스트로 적정값 탐색 |
| `maxLifetime` | HikariCP | 커넥션 최대 수명 | `wait_timeout`보다 짧게 설정 |
| `connectionTimeout` | HikariCP | 풀에서 커넥션 대기 최대 시간 | 사용자 경험 고려해 적절히 설정 |

---

## 6. 적절한 커넥션 수 찾기

### 프로세스

1. **모니터링 환경 구축** — CPU, Memory, RPS, 응답시간, 액티브 커넥션 수 추적 체계 마련
2. **부하 테스트 실행** — nGrinder 등 툴로 트래픽을 점진적으로 증가시키며 관찰
3. **병목 지점 파악** — 백엔드/DB 서버 리소스 & 스레드/커넥션 사용률 확인
4. **파라미터 조정** — 서버 증설 or `maximumPoolSize` / `max_connections` 증가
5. **반복 검증** — 조정 후 다시 부하 테스트 → 만족스러운 RPS, 응답시간 확인

### 관찰 지표 체크리스트

- [ ] RPS (Requests/Second) 추이
- [ ] 평균 응답 시간(ms)
- [ ] 백엔드 서버 CPU / Memory
- [ ] DB 서버 CPU / Memory
- [ ] 스레드풀 액티브 스레드 수
- [ ] DBCP 액티브 커넥션 수
- [ ] 에러 / 타임아웃 발생 여부

---

## 7. 실무 설정 예시

> 시나리오: 부하 테스트 결과 `max_connections = 60`이 적절, 백엔드 서버 확장 고려 중

**공식**: `백엔드 서버 수 × DBCP maximumPoolSize < MySQL max_connections (여유분 포함)`

| 구성 | DB 설정 | 서버당 풀 크기 | 총 커넥션 | 여유 |
|------|---------|--------------|----------|------|
| 백엔드 2대 | max_connections = 60 | 25 | 25 × 2 = 50 | 10개 |
| 백엔드 3대 | max_connections = 60 | 15 | 15 × 3 = 45 | 15개 |

> 예비 서버 추가를 대비해 `max_connections`에 충분한 여유를 두고 설정하는 것을 권장한다.

---

## 8. 실제 프로젝트 적용 — .NET OleDB 묵시적 풀링

### IndependentDbConnection (C# / Oracle OleDB)

이 프로젝트에서는 HikariCP 대신 **.NET의 OleDbConnection이 제공하는 묵시적 풀링**을 사용한다.

```csharp
// Open() 호출 시 .NET이 자동으로 풀에서 커넥션 재사용
_connection = new OleDbConnection(connectionString);
_connection.Open(); // 풀링 자동 적용

// Dispose() → 실제 TCP 종료가 아닌 풀 반납
public void Dispose()
{
    _connection.Close();   // → 풀에 반납 (TCP 미종료)
    _connection.Dispose();
}

// 사용 패턴 (using = 자동 Dispose 보장)
using (var db = new IndependentDbConnection())
{
    DataTable dt = db.ExecuteDataTable(sql, bind);
} // ← Dispose() 자동 호출 → 풀 반환
```

### HikariCP vs .NET OleDB 비교

| 항목 | HikariCP (Java) | OleDB (.NET) |
|------|----------------|--------------|
| 풀링 방식 | 명시적 설정 | 자동 (묵시적) |
| 풀 크기 설정 | `max-pool-size` 등 직접 설정 | 연결 문자열 기반, 자동 관리 |
| `Close()` 동작 | 풀 반납 | 풀 반납 (동일!) |
| `maxLifetime` | 직접 설정 가능 | 자동 관리 |
| 모니터링 | JMX / 상세 가능 | 기본 제공 없음 |
| 주 사용 언어 | Java (Spring) | C# (.NET) |
| DB | MySQL / 범용 | Oracle |

**핵심은 동일**: `Open()` = 풀에서 대여, `Close()` = 풀에 반납.  
다만 .NET OleDB는 설정 없이 자동 적용, HikariCP는 명시적으로 세밀하게 제어 가능.


---

## 참고

- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Naver D2 - Commons DBCP 이해하기](https://d2.naver.com/helloworld/5102792)
- [MySQL Server System Variables](https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html)
