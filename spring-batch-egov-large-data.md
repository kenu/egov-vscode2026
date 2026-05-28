# Spring Batch와 eGovFrame을 활용한 대용량 데이터 처리

> 발표자: 위승빈 | Issue #21

---

## 1. 발표 개요

### 학습 목표
- 대용량 데이터 처리 시 단순 for문이 왜 실패하는지 이해한다
- Spring Batch의 핵심 구조(Job → Step → Chunk)를 설명할 수 있다
- eGovFrame 환경에서 Spring Batch를 설정하고 실행한다

### 대상
전자정부 표준프레임워크 기반 프로젝트에서 배치 처리를 도입하려는 개발자

---

## 2. 문제 인식: 대용량 데이터 처리의 함정

### 흔한 실수 패턴

```java
// ❌ 100만 건을 한 번에 메모리에 올리는 방식
List<User> users = userRepository.findAll(); // OutOfMemoryError 위험
for (User user : users) {
    process(user);
    userRepository.save(user);
}
```

### 실제로 발생하는 문제
| 상황 | 문제 |
|------|------|
| 100만 건 조회 | `OutOfMemoryError` |
| 처리 중 장애 | 어디까지 처리됐는지 알 수 없음 |
| 재실행 | 중복 처리 발생 |
| 성능 | 단건 INSERT × 100만 번 → DB 부하 폭주 |

---

## 3. Spring Batch 핵심 구조

```
Job
 └─ Step
      └─ Chunk<Input, Output>
           ├─ ItemReader   : 데이터를 읽는다 (한 건씩)
           ├─ ItemProcessor: 변환/검증 처리 (선택)
           └─ ItemWriter   : 묶어서 저장 (chunkSize 단위)
```

### Chunk 처리 방식이 핵심

```
[Read → Process] × N건  →  [Write 한 번]  →  커밋
[Read → Process] × N건  →  [Write 한 번]  →  커밋
...
```

- 메모리 사용량을 `chunkSize`로 제어
- 청크 단위로 트랜잭션 → 장애 시 해당 청크만 롤백
- `JobRepository`가 실행 이력 관리 → 재시작 가능

---

## 4. eGovFrame + Spring Batch 설정

### pom.xml 의존성

```xml
<!-- Spring Batch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>

<!-- 데모용 인메모리 DB -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### application.properties 핵심 설정

```properties
# 애플리케이션 시작 시 Job 자동 실행 방지 (운영에서는 false 권장)
spring.batch.job.enabled=false

# H2 콘솔 (개발용)
spring.h2.console.enabled=true

# Batch 메타 테이블 자동 생성
spring.batch.jdbc.initialize-schema=always
```

### BatchConfig.java — Job 전체 구성

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job userMigrationJob(JobRepository jobRepository, Step userMigrationStep) {
        return new JobBuilder("userMigrationJob", jobRepository)
                .start(userMigrationStep)
                .build();
    }

    @Bean
    public Step userMigrationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<UserCsvDto> reader,
            ItemProcessor<UserCsvDto, User> processor,
            ItemWriter<User> writer) {

        return new StepBuilder("userMigrationStep", jobRepository)
                .<UserCsvDto, User>chunk(1000, transactionManager) // 1000건 단위 커밋
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
```

---

## 5. ItemReader / ItemProcessor / ItemWriter 구현

### ItemReader — CSV 파일 읽기

```java
@Bean
@StepScope
public FlatFileItemReader<UserCsvDto> csvReader(
        @Value("#{jobParameters['filePath']}") String filePath) {

    return new FlatFileItemReaderBuilder<UserCsvDto>()
            .name("userCsvReader")
            .resource(new FileSystemResource(filePath))
            .delimited()
            .names("id", "name", "email", "department")
            .targetType(UserCsvDto.class)
            .linesToSkip(1) // 헤더 스킵
            .build();
}
```

### ItemProcessor — 유효성 검증 + 변환

```java
@Component
public class UserItemProcessor implements ItemProcessor<UserCsvDto, User> {

    @Override
    public User process(UserCsvDto dto) {
        // null 또는 빈 이메일은 건너뜀 (null 반환 = skip)
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            return null;
        }
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail().toLowerCase())
                .department(dto.getDepartment())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
```

### ItemWriter — JdbcBatchItemWriter로 벌크 INSERT

```java
@Bean
public JdbcBatchItemWriter<User> jdbcWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<User>()
            .dataSource(dataSource)
            .sql("""
                INSERT INTO users (name, email, department, created_at)
                VALUES (:name, :email, :department, :createdAt)
                """)
            .beanMapped()
            .build();
}
```

> `JdbcBatchItemWriter`는 내부적으로 `PreparedStatement.addBatch()`를 사용해
> `chunkSize`만큼 모아서 한 번에 INSERT — 단건 INSERT 대비 수십 배 빠름

---

## 6. 처리 흐름 요약

```
애플리케이션 시작
    ↓
JobLauncher.run(userMigrationJob, jobParameters)
    ↓
Step 시작 → 청크 반복
    ┌─────────────────────────────────┐
    │ Read × 1000  →  Process × 1000  │
    │ → Write(BULK INSERT) → 커밋     │ ← 청크 1
    └─────────────────────────────────┘
    ┌─────────────────────────────────┐
    │ Read × 1000  →  Process × 1000  │
    │ → Write(BULK INSERT) → 커밋     │ ← 청크 2
    └─────────────────────────────────┘
    ...
    ↓
Step 완료 → Job 완료 → 이력 저장(JobRepository)
```

---

## 7. eGovFrame 환경 적용 시 주의사항

### eGovFrame 4.x 이하 (Spring XML 기반)

```xml
<!-- egov-batch-context.xml -->
<bean id="userMigrationJob"
      class="org.springframework.batch.core.job.SimpleJob">
    <property name="steps">
        <list>
            <ref bean="userMigrationStep"/>
        </list>
    </property>
    <property name="jobRepository" ref="jobRepository"/>
</bean>
```

### eGovFrame 5.0 (Spring Boot 기반) → 본 데모 방식 그대로 사용 가능

| 항목 | 4.x | 5.0 |
|------|-----|-----|
| 설정 방식 | XML | Java Config (@Configuration) |
| Spring Batch 버전 | 4.x | 5.x |
| Job 파라미터 타입 | `String` | `String / Long / Double / LocalDate` |
| `@EnableBatchProcessing` | 필수 | 선택 (auto-config 동작) |

---

## 8. 성능 최적화 포인트

### chunkSize 튜닝
```java
// 너무 작으면 커밋 횟수 증가 → DB 부하
// 너무 크면 메모리 부족 → OOM
// 일반적으로 500 ~ 5000 사이에서 테스트
.<UserCsvDto, User>chunk(1000, transactionManager)
```

### 병렬 처리 (Partitioning)

```java
@Bean
public Step partitionedStep(JobRepository jobRepository, PartitionHandler partitionHandler) {
    return new StepBuilder("partitionedStep", jobRepository)
            .partitioner("workerStep", partitioner())
            .partitionHandler(partitionHandler)
            .build();
}
```

- 파티셔너가 데이터를 N개 범위로 나눔
- 각 파티션을 스레드가 병렬 처리
- 주의: Writer가 thread-safe해야 함

---

## 9. 마무리

### 핵심 정리

| 개념 | 요점 |
|------|------|
| Chunk | 메모리 제어의 핵심. `chunkSize` = 한 트랜잭션 단위 |
| ItemReader | 데이터 소스에서 한 건씩 읽음 (DB, CSV, XML 등) |
| ItemProcessor | 변환/검증. `null` 반환 시 해당 건 skip |
| ItemWriter | 청크 단위 벌크 처리. `JdbcBatchItemWriter` 활용 |
| JobRepository | 실행 이력 관리. 재시작·중복 실행 방지 |

### 실무 체크리스트
- [ ] `chunkSize`를 환경에 맞게 튜닝했는가
- [ ] 장애 시 재시작 전략(restartable)을 정의했는가
- [ ] `ItemProcessor`에서 예외 skip/retry 정책을 설정했는가
- [ ] 배치 실행 시간이 다음 배치 시작 전에 끝나는가 (Scheduler 연동 시)
- [ ] `JobParameters`에 실행 시각을 포함해 매번 새 JobInstance를 생성하는가
