# eGovFrame 표준 프레임워크와 MyBatis/JPA 혼용 전략


---

## 1. 발표 개요 (Introduction)
* **주제:** eGovFrame 환경에서 MyBatis와 JPA를 함께 사용하는 하이브리드 아키텍처 전략
* **배경:** 공공/엔터프라이즈 시장의 표준인 eGovFrame은 전통적으로 MyBatis(iBatis)를 주로 사용해왔으나, 최근 객체지향적 개발과 생산성 향상을 위해 JPA의 도입 요구가 증가하고 있습니다.
* **목표:** 두 기술 중 하나만을 고집하는 대신, 각 기술의 장점을 극대화하여 실무에 적용하는 최적의 혼용 가이드라인을 제시합니다.

## 2. 기존 방식의 한계와 새로운 패러다임
### MyBatis 중심 개발의 한계
* 단순/반복적인 CRUD SQL 작성에 많은 시간 소요
* 테이블 중심 설계로 인한 객체 지향 모델링의 한계

### JPA 전면 도입 시의 부담
* 레거시 DB 구조나 통계/리포트성 복잡한 쿼리 작성의 어려움 (JPQL, QueryDSL의 복잡성)
* 높은 초기 러닝 커브와 DBA와의 협업 과정에서의 마찰

### 💡 해결책: MyBatis + JPA 혼용 아키텍처
* **JPA (Spring Data JPA):** 핵심 도메인 모델링, 상태 변경, 단순/반복 CRUD
* **MyBatis:** 복잡한 다중 조인, 동적 쿼리, 통계/배치 쿼리, 네이티브 SQL 튜닝이 필요한 영역

### 🔍 QueryDSL vs MyBatis+JPA 혼용 (혼용의 이점)
> **"트렌드인 QueryDSL 대신 왜 굳이 MyBatis를 섞어 쓸까?"**
순수 기술적(타입 안정성 등)으로는 QueryDSL이 우수할 수 있습니다. 하지만 수백 줄의 레거시 튜닝 쿼리와 DBA 협업이 필수적인 eGovFrame(공공/엔터프라이즈) 실무 환경에서는 **MyBatis 혼용**이 개발 리스크를 최소화하는 가장 현실적이고 실용적인 대안입니다.

* **완만한 학습 곡선과 즉시 전력화:** QueryDSL은 초기 설정(Q-Class 등)과 새로운 문법 학습에 시간이 필요하지만, MyBatis는 기존 개발자들에게 이미 익숙하므로 프로젝트 투입 즉시 복잡한 쿼리에 대응할 수 있습니다.
* **DBA 협업 및 SQL 튜닝 용이성:** DBA나 데이터 팀이 튜닝해 준 복잡한 네이티브 SQL(Native SQL)을 QueryDSL의 자바 코드로 재작성하는 것은 비효율적입니다. MyBatis는 튜닝된 SQL 원문을 XML에 그대로 복사하여 사용할 수 있습니다.
* **유연한 레거시 데이터 매핑:** 정규화가 부족한 레거시 테이블이나, 엔티티(Entity)로 매핑하기 까다로운 복합 통계 화면의 데이터를 DTO로 추출할 때 MyBatis가 훨씬 직관적입니다.

---

## 3. 핵심 혼용 전략 및 구현 포인트

### 3-1. 트랜잭션 관리 (Transaction Management)
* **이슈:** JPA의 영속성 컨텍스트(1차 캐시)와 MyBatis의 실행 컨텍스트 불일치 문제
* **해결:** `JpaTransactionManager`를 메인 트랜잭션 매니저로 설정합니다. Spring의 트랜잭션 동기화 매니저를 통해 MyBatis도 동일한 트랜잭션에 참여할 수 있습니다.

### 3-2. 영속성 컨텍스트 동기화 (Flush 타이밍)
* **이슈:** JPA로 엔티티를 수정한 직후, MyBatis로 해당 데이터를 조회하면 DB에 반영되지 않은 과거 데이터가 조회될 수 있습니다. (MyBatis는 JPA의 1차 캐시를 모름)
* **해결:** MyBatis 쿼리를 실행하기 직전에 반드시 **JPA 영속성 컨텍스트를 강제로 플러시(`entityManager.flush()`)** 하거나, Spring Data JPA의 `@Modifying(clearAutomatically = true)` 등을 적절히 활용해야 합니다.

### 3-3. DTO(Data Transfer Object) 분리
* JPA의 Entity를 MyBatis의 파라미터나 결과 타입으로 직접 사용하지 말고, 별도의 DTO를 구성하여 결합도를 낮추는 것이 좋습니다.

---

## 4. VS Code 기반 eGovFrame 프로젝트 개발 환경

전자정부 프레임워크는 주로 Eclipse 기반의 eGovFrame IDE를 제공하지만, **VS Code**를 활용하면 훨씬 가볍고 모던한 환경에서 개발할 수 있습니다.

### 4-1. 필수 확장 프로그램 (Extensions)
* **Extension Pack for Java:** Java 개발의 핵심 (언어 지원, 디버깅, 메이븐/그레이들 지원)
* **Spring Boot Extension Pack:** Spring 프레임워크 기반인 eGovFrame 프로젝트 설정 및 구동에 필수
* **MyBatisX:** MyBatis 인터페이스와 XML Mapper 간의 빠른 이동, 코드 자동 완성, 문법 검사 지원

### 4-2. 프로젝트 셋업 및 팁
> 💡 **Tip: 왜 Spring Initializr를 사용하나요?**
> Eclipse와 달리 VS Code에는 공식적인 'eGovFrame 프로젝트 생성 마법사' 플러그인이 없습니다. 따라서 VS Code 내부의 `Spring Initializr` 확장 기능(명령 팔레트)을 사용해 표준 프로젝트를 먼저 만든 뒤 프레임워크 라이브러리를 얹는 방식을 사용합니다.

1. **프로젝트 초기화:** VS Code의 `Spring Initializr`를 통해 필수 의존성(`Spring Web`, `Spring Data JPA`, `MyBatis Framework`, 사용 DB 드라이버)을 선택하여 프로젝트를 생성한 후, eGovFrame 전용 핵심 라이브러리(`egovframework.rte.*`)를 `pom.xml` 또는 `build.gradle`에 수동 추가합니다.
2. **application.properties 설정:** JPA와 MyBatis가 같은 DB에 접근하도록 datasource를 하나로 두고, 테이블 생성과 초기 데이터는 `schema.sql`, `data-local.sql`로 명시합니다. 예제에서는 SQLite를 사용하므로 Hibernate SQLite Dialect를 함께 설정합니다.
   ```properties
   mybatis.mapper-locations=classpath:mapper/**/*.xml

   spring.datasource.driver-class-name=org.sqlite.JDBC
   spring.datasource.url=jdbc:sqlite:local.db

   spring.sql.init.mode=always
   spring.sql.init.schema-locations=classpath:schema.sql
   spring.sql.init.data-locations=classpath:data-local.sql

   spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
   spring.jpa.hibernate.ddl-auto=none
   spring.jpa.show-sql=true
   ```

### 4-3. 템플릿 코드 실행 및 테스트 방법
서버를 구동한 뒤, 제공된 API를 통해 JPA와 MyBatis가 어떻게 혼용되어 동작하는지 직접 테스트해 볼 수 있습니다.

1. **서버 실행하기**
   * VS Code 터미널에서 `cd myBatis-JPA-presentation` 경로로 이동한 후, `mvn spring-boot:run` 명령어를 실행하거나 `DemoApplication.java`에서 `Run`을 클릭하여 서버를 구동합니다. (기본 포트: 8080)
   * 서버 구동 시 `data-local.sql`에 의해 초기 데이터(1번, 2번 회원)가 자동 세팅됩니다.

2. **API 테스트 (cURL 또는 브라우저 활용)**
   * **[JPA] 회원 목록 조회 (GET)**
     * 브라우저에서 `http://localhost:8080/members` 접속
     * `findAll()`을 통해 초기 회원 목록이 JSON으로 출력되는 것을 확인합니다.
   * **[JPA + MyBatis] 회원 이름 변경 및 통계 조회 (PATCH)**
     * 터미널에서 아래 명령어를 실행하여 1번 회원의 이름을 변경해 봅니다.
       ```bash
       curl -X PATCH "http://localhost:8080/members/1/name?newName=TestUser"
       ```
     * **💡 핵심 확인 포인트:** VS Code 하단 콘솔 창에서 **JPA의 `UPDATE` 쿼리가 먼저 실행(Flush)**된 직후, **MyBatis의 `SELECT` 조인 쿼리가 실행**되는 것을 눈으로 직접 확인하세요!
   * **[JPA 벌크 + MyBatis] 회원 탈퇴 처리 및 통계 조회 (DELETE)**
     * 터미널에서 아래 명령어로 2번 회원을 탈퇴(Soft Delete) 처리합니다.
       ```bash
       curl -X DELETE "http://localhost:8080/members/2"
       ```
     * **💡 핵심 확인 포인트:** 영속성 컨텍스트(1차 캐시)를 우회하여 DB로 다이렉트 `UPDATE`를 쏘는 `@Modifying` 벌크 쿼리가 실행됩니다. 이때 `clearAutomatically = true`, `flushAutomatically = true` 옵션 덕분에, **수동 Flush 호출 없이도** 곧바로 이어지는 MyBatis `SELECT` 쿼리가 최신 데이터(`"isDeleted": true`)를 완벽하게 읽어와 응답하는 것을 확인하세요!

---

## 5. 기대 효과 및 마무리
* **개발 생산성 극대화:** 지루한 CRUD는 JPA에 맡기고, 개발자는 비즈니스 로직에 집중
* **성능 및 유지보수성 확보:** 복잡한 쿼리는 MyBatis를 통해 SQL을 직접 제어하고 튜닝
* **결론:** 실무에서는 은탄환(Silver Bullet)이 없습니다. 서비스의 특성과 데이터의 성격에 맞게 JPA와 MyBatis를 적재적소에 활용하는 하이브리드 전략이 가장 현실적이고 효율적인 대안입니다.
