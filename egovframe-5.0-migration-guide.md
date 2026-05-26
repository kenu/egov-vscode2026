# eGovFrame 5.0(Spring Boot 기반) 마이그레이션 가이드

## 1. 주제 선정 배경: 왜 5.0 마이그레이션인가?

### 1.1 원래 주제: `@Valid`와 `BindingResult`를 활용한 Validation
당초 계획했던 발표 주제는 **Validation 프레임워크 활용: @Valid와 BindingResult 처리**였습니다. 입력 값 검증은 웹 애플리케이션의 기본이면서도 실무에서 자주 놓치거나 복잡하게 구현되는 부분이기 때문입니다.

### 1.2 주제를 변경하게 된 결정적 계기: RFP(제안요청서)
최근 회사에서 새로운 프로젝트의 **RFP(제안요청서)** 를 검토하던 중 다음과 같은 요구사항을 발견했습니다.
> **"최신 버전의 전자정부표준프레임워크(eGovFrame)를 적용하여 시스템을 구축해야 함."**

이 문구를 보는 순간, 단순한 기능 구현 방법론보다 더 시급하고 중요한 문제가 떠올랐습니다.

### 1.3 "어... 이거 5.0부터 Jakarta EE인데 문제되지 않나?"
전자정부프레임워크 5.0은 **Spring Boot 3.x** 를 기반으로 하며, 가장 큰 변화는 기존의 `javax.*` 패키지에서 **`jakarta.*` 패키지로의 전환(Jakarta EE)** 입니다. 단순히 버전 숫자가 올라가는 것이 아니라, 소스 코드 레벨의 임포트(Import) 구문을 모두 수정해야 하고, 기존에 사용하던 수많은 오픈소스 라이브러리와의 호환성 문제가 발생할 것이 명확했습니다.

### 1.4 5.0으로의 결정
사실 멘토님께서 AI로 제안해 주셨던 마이그레이션 관련 주제는 **4.x 버전**이었습니다. 하지만 다음과 같은 이유로 최종적으로 **5.0 마이그레이션**을 정면으로 다루기로 했습니다.

*   **경험:** "할 거면 제대로, 최신 버전으로 해보자"는 판단이 섰습니다. RFP에서 요구하는 '최신 버전'은 결국 5.0을 의미하기 때문입니다.
*   **미래 지향적 선택:** 4.x에서 5.0으로 넘어가는 과정이야말로 가장 도전적이고 실무적인 고민이 많이 담긴 핵심 과정이라 생각했습니다.

따라서, 단순히 API 사용법을 익히는 것보다 **실무 프로젝트에서 마주하게 될 '5.0 마이그레이션'의 현실적인 문제점과 해결 방안**을 공유하는 것이 동료 개발자들에게 훨씬 가치 있는 정보가 될 것이라 판단하여 주제를 변경하게 되었습니다.

## 2. eGovFrame 5.0(Spring Boot 3.x) 무엇이 바뀌었나?

단순한 버전 업그레이드를 넘어, 자바 생태계와 스프링 프레임워크의 거대한 패러다임 변화를 담고 있습니다.

### 2.1 Java 17/21: 현대적 자바의 시작
*   **Java 17 (실행환경):** Spring Boot 3.0부터 Java 17이 최소 버전이 되었습니다. 레코드(Record), 텍스트 블록(Text Blocks), Switch 표현식 등 생산성을 높여주는 기능들이 표준이 되었습니다.
*   **Java 21 (개발환경):** eGovFrame 5.0은 Java 21을 완벽히 지원합니다.
    *   **Virtual Threads (가상 스레드):** **Java 21**의 핵심입니다. 기존의 무거운 플랫폼 스레드 대신 수백만 개의 스레드를 가볍게 생성할 수 있어, 높은 동시성을 요구하는 서버 애플리케이션의 처리 능력을 획기적으로 향상시킵니다.
    *   **트렌드의 변화:** 최근 많은 기업들이 비동기 논블로킹 방식의 복잡한 **Spring WebFlux**에서 벗어나, 익숙한 명령형 프로그래밍 모델을 유지하면서도 성능을 챙길 수 있는 **Virtual Threads**로 넘어가는 추세입니다. 
    *   **실제 사례:** 특히 리액티브 프로그래밍의 선두주자였던 **넷플릭스(Netflix)** 조차 가상 스레드 도입 이후 복잡한 비동기 스택을 걷어내고 더 단순하고 직관적인 코드로 회귀하고 있다는 점은 매우 상징적입니다. 전자정부프레임워크 5.0이 Java 21을 적극 권장하는 이유도 이러한 글로벌 기술 트렌드와 궤를 같이합니다.
아무래도 여전히 구버전을 쓰는 사람들을 위해 실행환경은 17부터 지원하는 거 아닐까 라는 추측을 해보았습니다.

### 2.2 Spring Boot 3.x 기반의 핵심 변화
*   **Jakarta EE 10 지원:** 내장 서버(Tomcat 10, Jetty 11 등)가 Jakarta EE 10을 지원하도록 업그레이드되었습니다.
*   **Auto-Configuration 방식 변경:** `META-INF/spring.factories` 방식이 폐지되고, `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일에 선언하는 방식으로 변경되었습니다.
*   **GraalVM Native Image 지원:** 애플리케이션 시작 속도가 획기적으로 빨라지고 메모리 사용량이 줄어듭니다.
*   **Observability (관측 가능성):** Micrometer를 통한 추적(Tracing) 및 지표(Metrics) 수집이 프레임워크 수준에서 더 정교해졌습니다.

### 2.3 Jakarta EE 전환 (Namespace의 변화)
*   **javax.* → jakarta.*:** 이것이 마이그레이션의 가장 큰 장벽입니다.
*   `javax.servlet.*`, `javax.persistence.*`, `javax.validation.*` 등 우리가 쓰던 모든 기본 패키지를 `jakarta.*`로 바꿔야 합니다.

### 2.4 핵심 라이브러리 연쇄 업그레이드
*   **Hibernate 6.x:** 쿼리 생성 방식과 성능 최적화가 이루어졌습니다.
*   **Spring Security 6.x:** 설정 방식이 `WebSecurityConfigurerAdapter` 상속 방식에서 컴포넌트 기반(Lambda식) 설정으로 완전히 변경되었습니다.

## 3. [핵심] 실전 마이그레이션 여정: 4.3 MVC에서 5.0 Boot로

실제 현장에서 가장 많이 쓰이는 **eGovFrame 4.3 (Spring MVC, WAR, Java 8)** 기반의 구형 프로젝트를 **eGovFrame 5.0 (Spring Boot 3.x, JAR, Java 21)** 로 직접 마이그레이션하며 겪은 생생한 에러와 해결 과정(Modernization)을 정리했습니다.

### 3.1 1단계: 빌드 환경 최신화 (pom.xml 수술)
가장 먼저 프로젝트의 엔진을 교체하는 과정입니다. 기존의 메타데이터(라이선스, 기존 라이브러리)는 유지하되, **최소한의 필수 설정만 핀포인트로 수정**했습니다.

1.  **Spring Boot 엔진 장착:** `<parent>`에 `spring-boot-starter-parent` (3.2.5) 추가.
2.  **버전 상향:** `<java.version>`을 21로, `<org.egovframe.rte.version>`을 5.0.0으로 상향.
3.  **Servlet API 교체:** `javax.servlet` 의존성을 **`jakarta.servlet`**으로 변경 (Jakarta EE 10 대응).
4.  **🚨 에러 1: 의존성 인식 실패 (Artifact ID 변경)**
    *   **문제:** 메이븐이 `org.egovframe.rte.ptl.mvc` 등 5.0.0 라이브러리를 찾지 못함.
    *   **해결:** 5.0.0부터 전자정부 라이브러리 명명 규칙이 점(`.`)에서 하이픈(`-`)으로 변경됨 (`egovframe-rte-ptl-mvc`). 이를 반영하여 `pom.xml` 전면 수정.

### 3.2 2단계: 코드 레벨 대공사 (컴파일 에러 해결)
POM을 수정하고 `mvn clean compile`을 실행하자 소스 코드 곳곳에서 에러가 터졌습니다.

1.  **🚨 에러 2: 구형 Validation 퇴출**
    *   **문제:** `org.springmodules.validation.commons.DefaultBeanValidator` 클래스 찾을 수 없음.
    *   **원인:** 해당 라이브러리는 Spring 6(Jakarta EE 10)와 호환되지 않아 전자정부 5.0에서 퇴출됨.
    *   **해결:** `EgovSampleController.java` 등에서 구형 검증기 의존성 및 호출 코드를 모두 제거. (향후 표준 `jakarta.validation`으로 교체 필요)
2.  **🚨 에러 3: MyBatis Mapper 미인식**
    *   **문제:** `org.egovframe.rte.psl.dataaccess.mapper.Mapper` 어노테이션 없음.
    *   **원인:** 프레임워크 전용 어노테이션이 제거되고 오픈소스 표준을 직접 쓰도록 가이드가 변경됨.
    *   **해결:** `SampleMapper.java`의 임포트를 MyBatis 표준인 `org.apache.ibatis.annotations.Mapper`로 교체.
3.  **🚨 에러 4: 패키지명 불일치 (The Great Rename)**
    *   **문제:** `ServletContextAware` 등의 스프링 인터페이스가 `jakarta.servlet`을 요구.
    *   **해결:** `EgovImgPaginationRenderer.java` 등 프로젝트 전반의 `import javax.servlet.*`을 **`jakarta.servlet.*`**으로 일괄 변경하여 컴파일 최종 성공.

### 3.3 3단계: Spring Boot Modernization (런타임 에러 해결)
컴파일은 성공했지만, 외부 톰캣에 얹혀가던(WAR) 프로젝트를 자체 실행 가능한 부트(JAR)로 가동(`mvn spring-boot:run`)하자 런타임 에러들이 발생했습니다.

1.  **부트 진입점 생성:** `MigrationApplication.java` (Main 클래스) 신규 생성 및 `@SpringBootApplication` 적용.
2.  **🚨 에러 5: WEB-INF 리소스 접근 불가 (FileNotFound)**
    *   **문제:** 부트가 `WEB-INF/config/.../dispatcher-servlet.xml`을 찾지 못함.
    *   **원인:** 부트(JAR)는 `WEB-INF`를 무시하고 `src/main/resources`만 클래스패스로 인식.
    *   **해결:** XML 설정 파일들을 `src/main/resources/egovframework/config/`로 물리적으로 이동시키고 `@ImportResource` 경로 수정.
3.  **🚨 에러 6: 빈 이름 중복 (BeanDefinitionOverride)**
    *   **문제:** 부트의 자동 설정(Auto-Configuration)과 기존 XML 설정 간 빈 이름 충돌.
    *   **해결:** `application.properties` 생성 후 `spring.main.allow-bean-definition-overriding=true` 설정 추가 (과도기적 조치).
4.  **🚨 에러 7: XML 내 구형 클래스 잔재**
    *   **문제:** 자바 코드에서는 지웠던 `DefaultBeanValidator`를 XML에서 계속 로딩하려다 실패.
    *   **해결:** `target` 폴더를 지우는 `mvn clean` 수행 및 불필요해진 `context-validator.xml` 파일 완전 삭제.
5.  **🚨 에러 8: MyBatis 빈 주입 실패**
    *   **문제:** 서비스 클래스에서 `SampleMapper` 주입 실패.
    *   **원인:** 표준 `@Mapper`를 사용하면서 스프링 부트용 스캔 설정이 누락됨.
    *   **해결:** 메인 클래스에 `@MapperScan` 어노테이션을 추가하여 매퍼 위치 명시.
6.  **🚨 에러 9: JSP 및 JSTL 렌더링 실패**
    *   **문제 1 (JSTL):** `NoClassDefFoundError: jakarta/servlet/jsp/jstl/core/Config`
    *   **해결 1:** `pom.xml`에서 구형 JSTL 제거 후 Jakarta EE 10 호환 JSTL(3.0) 라이브러리로 교체.
    *   **문제 2 (JSP 404):** 부트는 기본적으로 JSP 엔진이 없음.
    *   **해결 2:** `pom.xml`에 `tomcat-embed-jasper` 추가 및 `properties`에 View Resolver(prefix/suffix) 설정.
7.  **🚨 에러 10: 구형 Taglib 참조 에러 (JasperException)**
    *   **문제:** JSP 파일 상단에 구형 Validation 태그 라이브러리 선언이 남아있어 화면 렌더링 실패.
    *   **해결:** `validator.jsp` 내용 삭제 및 `egovSampleRegister.jsp` 내의 `<%@ taglib prefix="validator"... %>` 및 관련 스크립트 완전 제거.

이 과정을 거쳐 마침내 **"eGovFrame 4.3 MVC 프로젝트를 5.0 Spring Boot 프로젝트로 완벽하게 띄우는 데 성공"** 했습니다.

## 4. 맺음말: 두려움보다 기대감이 큰 마이그레이션

### 4.1 마이그레이션이 주는 가치
초반의 '패키지명 바꾸기'나 '라이브러리 호환성 해결' 과정은 분명 고통스럽지만, Java 21 가상 스레드를 통한 성능 향상과 최신 보안 패치 적용이라는 확실한 보상이 기다리고 있습니다.

### 4.2 준비하며 느낀 점: "전자정부프레임워크가 변하고 있다"
이번 5.0 마이그레이션 실습을 직접 진행하며, 전자정부프레임워크가 기술 트렌드를 매우 공격적으로 수용하고 있다는 인상을 받았습니다. 저를 포함한 많은 개발자들이 Java 11~17 혹은 레거시 Spring MVC에 머물러 있지만, 프레임워크의 이러한 변화는 우리에게도 새로운 기술적 도전과 자극이 될 것입니다.

최신 전자정부프레임워크 적용은 단순히 RFP를 준수하는 것을 넘어, 시스템의 지속 가능성을 높이고 개발자로서 한 단계 성장하는 진정한 마이그레이션의 계기가 될 것입니다.
