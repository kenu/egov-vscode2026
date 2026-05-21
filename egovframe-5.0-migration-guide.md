# eGovFrame 5.0(Spring Boot 기반) 마이그레이션 가이드

## 1. 주제 선정 배경: 왜 5.0 마이그레이션인가?

### 1.1 원래 주제: `@Valid`와 `BindingResult`를 활용한 Validation
당초 계획했던 발표 주제는 **Validation 프레임워크 활용: @Valid와 BindingResult 처리**였습니다. 입력 값 검증은 웹 애플리케이션의 기본이면서도 실무에서 자주 놓치거나 복잡하게 구현되는 부분이기 때문입니다.

### 1.2 주제를 변경하게 된 결정적 계기: RFP(제안요청서)
최근 회사에서 새로운 프로젝트의 **RFP(제안요청서)** 를 검토하던 중 다음과 같은 요구사항을 발견했습니다.
> **"최신 버전의 전자정부표준프레임워크(eGovFrame)를 적용하여 시스템을 구축해야 함."**

이 문구를 보는 순간, 단순한 기능 구현 방법론보다 더 시급하고 중요한 문제가 떠올랐습니다.

### 1.3 "어... 이거 5.0부터 Jakarta EE인데 문제되지 않나?"
전자정부프레임워크 5.0은 **Spring Boot 3.x**를 기반으로 하며, 가장 큰 변화는 기존의 `javax.*` 패키지에서 **`jakarta.*` 패키지로의 전환(Jakarta EE)**입니다. 단순히 버전 숫자가 올라가는 것이 아니라, 소스 코드 레벨의 임포트(Import) 구문을 모두 수정해야 하고, 기존에 사용하던 수많은 오픈소스 라이브러리와의 호환성 문제가 발생할 것이 명확했습니다.

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

## 3. 마이그레이션 실전 전략: 단계별 가이드

성공적인 마이그레이션을 위한 체크리스트와 단계별 수행 절차입니다.

### 3.1 1단계: 환경 점검 및 빌드 도구 최신화
*   **JDK 설치:** 최소 Java 17이 필요하며, 가상 스레드 활용을 위해 **Java 21** 설치를 강력히 권장합니다.
*   **Maven 버전:** Maven 3.6.3 이상이 필요하며, 최신 플러그인 호환성을 위해 **3.8.x 이상**을 권장합니다.
*   **IDE 설정:** VS Code를 사용하는 경우, `Language Support for Java` 확장이 최신 버전인지 확인합니다.

### 3.2 2단계: `pom.xml` 의존성 및 버전 업데이트
가장 먼저 프로젝트의 심장인 빌드 설정부터 수정해야 합니다.
*   **eGovFrame 버전 상향:** `<egovframework.rte.version>5.0.0</egovframework.rte.version>`
*   **Artifact ID 명명 규칙 변경 (주의):** 5.0.0부터 라이브러리 식별자 형식이 점(`.`)에서 하이픈(`-`)으로 변경되었습니다.
    *   예: `org.egovframe.rte.ptl.mvc` -> **`egovframe-rte-ptl-mvc`**
    *   이 부분을 수정하지 않으면 라이브러리를 찾지 못해 빌드 에러가 발생합니다.
*   **Spring Boot Starters:** eGovFrame에서 제공하는 `egovframe-boot-starter-*` 의존성들이 Spring Boot 3.x와 호환되도록 업데이트되었는지 확인합니다.
*   **외부 라이브러리 체크:** `lombok`, `mapstruct`, `querydsl` 등 Jakarta EE를 지원하는 버전으로 모두 상향해야 합니다.

### 3.3 3단계: 패키지명 일괄 전환 (The Great Rename)
가장 물리적인 시간이 많이 소요되는 작업입니다.
*   **대상:** `javax.servlet.*`, `javax.persistence.*`, `javax.validation.*`, `javax.annotation.*` 등
*   **변경:** 모두 `jakarta.*`로 시작하도록 수정합니다.
*   **검증의 함정:** 때때로 `pom.xml`만 수정해도 빌드가 성공하는 경우가 있습니다. 이는 다른 라이브러리가 전이 의존성(Transitive Dependency)으로 구형 `javax` 패키지를 여전히 끌어오고 있기 때문일 수 있습니다. 하지만 진정한 5.0 마이그레이션을 위해서는 소스 코드 내의 모든 `javax` 참조를 명시적으로 `jakarta`로 전환해야 합니다.
*   **Tip (VS Code 활용):** `Ctrl + Shift + F` (전체 찾기) 후 일괄 바꾸기를 실행하되, `javax.sql`이나 `javax.crypto` 등 여전히 `javax`를 유지하는 패키지를 제외하도록 주의합니다.

### 3.4 [중요] 실전 에러 대응 가이드
`pom.xml` 수정 후 `mvn clean compile` 및 `mvn spring-boot:run` 시 마주하게 되는 대표적인 에러들과 해결 방안입니다.

#### ① 구형 Validation 라이브러리의 퇴출
*   **에러 메시지:** `package org.springmodules.validation.commons does not exist`
*   **원인:** 전자정부 4.x까지 쓰이던 `springmodules-validation`은 Jakarta EE 10 환경과 호환되지 않아 5.0에서 더 이상 지원되지 않습니다.
*   **해결:** 구형 `DefaultBeanValidator` 참조를 제거하고, 표준인 **`jakarta.validation` (Hibernate Validator)**으로 교체해야 합니다.

#### ② MyBatis Mapper 어노테이션 미인식
*   **에러 메시지:** `cannot find symbol: class Mapper (location: package org.egovframe.rte.psl.dataaccess.mapper)` 혹은 `UnsatisfiedDependencyException (No qualifying bean of type '...SampleMapper')`
*   **원인:** eGovFrame 5.0에서 전용 `@Mapper`가 제거됨에 따라 MyBatis 표준 `@Mapper`로 교체해야 하며, 스프링 부트 환경에서는 이 매퍼들을 빈으로 등록하기 위한 스캔 설정이 별도로 필요합니다.
*   **해결:** 
    1.  `import org.apache.ibatis.annotations.Mapper;`로 교체합니다.
    2.  메인 클래스에 `@MapperScan(basePackages = "...")` 설정을 추가하여 매퍼 인터페이스를 스프링 빈으로 등록해야 합니다.

#### ③ WEB-INF 리소스 접근 불가 (FileNotFound)
*   **에러 메시지:** `java.io.FileNotFoundException: class path resource [WEB-INF/.../dispatcher-servlet.xml] cannot be opened`
*   **원인:** 전통적인 WAR 방식에서는 `WEB-INF` 폴더가 웹 컨텍스트 루트에 있었으나, 스프링 부트의 JAR 실행 방식은 오직 **클래스패스(src/main/resources)** 내의 자원만 인식할 수 있습니다.
*   **해결:** `WEB-INF/config` 아래의 설정 파일들을 `src/main/resources` 하위 폴더로 이동시키고, `@ImportResource` 경로를 `classpath:/...` 형식으로 수정해야 합니다.

#### ④ 빈 중복 등록 에러 (BeanDefinitionOverride)
*   **에러 메시지:** `BeanDefinitionOverrideException: Invalid bean definition with name 'mvcUrlPathHelper'...`
*   **원인:** 스프링 부트 2.1부터는 빈 이름 중복 시 에러를 발생시키는 것이 기본값입니다. 기존 XML 설정과 부트의 자동 설정(Auto-Configuration)이 충돌할 때 주로 발생합니다.
*   **해결:** `application.properties`에 `spring.main.allow-bean-definition-overriding=true` 설정을 추가하여 중복 등록을 허용해 주어야 합니다.

#### ⑤ XML 설정 내 구형 라이브러리 잔재
*   **에러 메시지:** `Cannot find class [org.springmodules.validation.commons.DefaultBeanValidator] for bean with name 'beanValidator'`
*   **원인:** 자바 소스 코드에서 구형 라이브러리 참조를 제거했더라도, 기존의 **XML 설정 파일(`context-validator.xml` 등)** 내에 해당 클래스를 빈으로 등록하려는 설정이 남아있는 경우 발생합니다.
*   **해결:** 해당 XML 파일을 삭제하거나, 불필요한 빈 설정을 제거해야 합니다. 스프링 부트 기반에서는 가능하면 이러한 XML 설정을 부트의 자동 설정이나 Java Config로 대체하는 것을 권장합니다.

#### ⑥ JSTL 라이브러리 인식 불가 (NoClassDefFound)
*   **에러 메시지:** `java.lang.NoClassDefFoundError: jakarta/servlet/jsp/jstl/core/Config`
*   **원인:** `javax.servlet`을 `jakarta.servlet`으로 변경했음에도 불구하고, 화면 출력(JSP)에 사용되는 JSTL 라이브러리는 여전히 구형(javax 버전)을 사용하고 있어 발생하는 런타임 에러입니다.
*   **해결:** `pom.xml`에서 구형 JSTL 의존성을 제거하고, **`jakarta.servlet.jsp.jstl-api`**와 그 구현체(Glassfish 등)를 최신 버전(3.0 이상)으로 교체해야 합니다.

#### ⑦ JSP 화면 미출력 (404 에러 또는 WEB-INF 접근 경고)
*   **에러 메시지:** `WARN ... ResourceHttpRequestHandler : "Path with "WEB-INF" or "META-INF": ...`
*   **원인:** 스프링 부트는 기본적으로 JSP 엔진을 포함하지 않으며, JSP 뷰 리졸버 설정이 없으면 해당 경로를 정적 리소스로 오해하여 접근을 차단합니다.
*   **해결:** 
    1.  `pom.xml`에 **`tomcat-embed-jasper`** 의존성을 추가합니다.
    2.  `application.properties`에 `spring.mvc.view.prefix`와 `spring.mvc.view.suffix` 설정을 추가하여 JSP 파일의 위치를 명시합니다.

#### ⑧ 구형 Taglib 참조 에러 (JasperException)
*   **에러 메시지:** `The absolute uri: [http://www.springmodules.org/tags/commons-validator] cannot be resolved`
*   **원인:** `pom.xml`에서 제거한 구형 라이브러리(Spring Modules 등)를 참조하는 **Taglib 선언(`<%@ taglib ... %>`)**이 JSP 파일 내에 남아있을 때 발생합니다.
*   **해결:** 모든 JSP 파일을 검수하여 퇴출된 라이브러리와 관련된 Taglib 선언 및 커스텀 태그(예: `<validator:javascript>`)를 제거해야 합니다. 클라이언트 사이드 검증 로직이 깨질 수 있으므로 관련 자바스크립트 코드 수정도 병행해야 합니다.

### 3.5 4단계: 설정 파일 및 코드 수정
*   **Spring Security 6.x:** 컴포넌트 기반 및 람다식 설정으로 전환합니다.
*   **Spring Boot Property:** `application.properties` 내의 변경된 속성명(예: Redis, Thymeleaf 관련)을 확인하고 수정합니다.
*   **Modernization:** 기존의 XML 설정들을 최대한 Java Config 및 Properties로 통합하여 관리 효율성을 높입니다.

## 4. 맺음말: 두려움보다 기대감이 큰 마이그레이션

### 4.1 마이그레이션이 주는 가치
초반의 '패키지명 바꾸기'나 '라이브러리 호환성 해결' 과정은 분명 고통스럽지만, Java 21 가상 스레드를 통한 성능 향상과 최신 보안 패치 적용이라는 확실한 보상이 기다리고 있습니다.

### 4.2 준비하며 느낀 점: "전자정부프레임워크가 변하고 있다"
이번 5.0 마이그레이션을 준비하며 전자정부프레임워크가 기술 트렌드를 매우 공격적으로 수용하고 있다는 인상을 받았습니다. 저를 포함한 많은 개발자들이 Java 11~17에 머물러 있지만, 프레임워크의 이러한 변화는 우리에게도 새로운 기술적 도전과 자극이 될 것입니다.

최신 전자정부프레임워크 적용은 단순히 RFP를 준수하는 것을 넘어, 시스템의 지속 가능성을 높이고 개발자로서 한 단계 성장하는 계기가 될 것입니다.
