# Spring Batch + eGovFrame 대용량 데이터 처리 데모

CSV 파일을 읽어 H2 인메모리 DB에 저장하는 Spring Batch 청크 처리 예제입니다.

## 실행 방법

```bash
mvn spring-boot:run
```

실행 후 `http://localhost:8080/h2-console` 에서 처리 결과 확인 가능합니다.
(JDBC URL: `jdbc:h2:mem:testdb`)

## 프로젝트 구조

```
src/main/java/com/example/springbatchegov/
├── SpringBatchEgovApplication.java   # 진입점
├── batch/
│   ├── BatchConfig.java              # Job / Step 설정
│   ├── UserItemProcessor.java        # 변환/검증
│   └── UserItemWriter.java           # 벌크 INSERT
└── domain/
    ├── User.java                     # JPA 엔티티
    └── UserCsvDto.java               # CSV 매핑 DTO
```

## 샘플 CSV 위치

`src/main/resources/sample-users.csv`
