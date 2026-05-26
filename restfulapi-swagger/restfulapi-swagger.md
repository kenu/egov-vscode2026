
# eGovFrame 기반 RESTful API 설계 및 Swagger 연동

**발표자:** [김경화/5조]

---

## 목차

1. RESTful API 개요
2. RESTful API 설계 핵심 가이드
3. Swagger(OpenAPI) 도입의 필요성
4. eGovFrame 내 Swagger 연동 방법 (Spring Boot 기반)
5. 실무 적용 예시 (전자정부프레임워크 sample API)
6. 마무리 및 기대 효과

---

## 1. RESTful API 개요

**배경**
* 웹 애플리케이션 아키텍처가 전형적인 단일체(Monolithic) 구조에서 **상호 가용성**과 **확장성**을 중시하는 MSA(Microservices Architecture) 및 
React, Vue 등 프론트엔드 프레임워크와의 원활한 연동을 위한 Front-Back 분리 구조로 변화.

**역할**
* HTTP 프로토콜의 표준을 최대한 활용하여, 클라이언트와 서버 간의 독립적이고 명확한 통신 인터페이스를 제공.


* 다양한 클라이언트(웹, 모바일앱, 외부 시스템)에 동일한 데이터(JSON/XML)를 제공하는 허브 역할.

---

## 2. RESTful API 설계 핵심 가이드

* **리소스(Resource) 중심의 URI 설계**
* URI에는 명사를 사용하고 행위(동사)는 포함하지 않습니다.
* ❌ `POST /create/post`
* ⭕ `POST /post`


* **HTTP 메서드를 활용한 명확한 행위 정의**
* `GET`: 리소스 조회
* `POST`: 리소스 생성
* `PUT` / `PATCH`: 리소스 전체 / 부분 수정
* `DELETE`: 리소스 삭제

---

## 3. Swagger(OpenAPI) 도입의 필요성

* **문서의 자동화 및 동기화**
* 엑셀이나 위키로 API 명세서를 관리하면 코드와 문서가 불일치하는 '파편화' 문제가 발생합니다.
* Swagger는 코드에 작성된 어노테이션을 기반으로 문서를 자동 생성하므로 실시간 동기화가 보장됩니다.


* **직관적인 테스트 환경 (Swagger UI)**
* 브라우저 상에서 즉각적인 API 호출 및 파라미터 테스트가 가능하여 Postman 설정 시간을 단축합니다.


* **프론트엔드와의 협업 강화**
* 변경된 API 스펙이 즉시 공유되므로, SI 프로젝트 등에서 개발자 간 커뮤니케이션 비용이 크게 감소합니다.



---

## 4. eGovFrame 내 Swagger 연동 방법 (Spring Boot 기준)

* **1. 의존성 추가 (pom.xml)**
* Springdoc OpenAPI 라이브러리를 활용합니다.


```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>

```


* **2. Swagger 설정 클래스 작성 (SwaggerConfig.java)**

```java
  @Configuration
  public class SwaggerConfig {
      @Bean
      public OpenAPI openAPI() {
          return new OpenAPI()
                  .info(new Info()
                  .title("전자정부프레임워크 샘플 프로젝트 API 명세서")
                  .description("전자정부프레임워크 샘플 프로젝트 REST API 문서입니다.")
                  .version("v1.0.0"));
      }
  }

```

---

## 5. 실무 적용 예시 (전자정부프레임워크 샘플 프로젝트 API)


* **1. Swagger 어노테이션**

| 어노테이션 | 대상 | 역할 | 주요 속성 | 사용 예시 |
|---|---|---|---|---|
| `@Tag` | Controller | API 그룹(카테고리) 설명 | `name`, `description` | 사용자 API 그룹 정의 |
| `@Operation` | Method | 개별 API 설명 | `summary`, `description` | 사용자 조회 API 설명 |
| `@Schema` | DTO / Field | 요청·응답 모델 문서화 | `description`, `example`, `hidden`, `allowableValues` | DTO 필드 설명 및 예시값 |
| `@ParameterObject` | Query DTO Parameter | Query Parameter 자동 매핑 및 문서화 | 별도 속성 없음 | 검색 조건 DTO를 query parameter로 노출 |


* **2. 적용 예시**

```java

@Tag(name = "EgovSample", description = "샘플 게시판 관리 API (MVC)")
@Controller
@Slf4j
public class EgovSampleController {

	@Operation(summary = "루트 페이지 이동", description = "인덱스 페이지 접속 시 글 목록 화면으로 이동한다.")
	@GetMapping("/")
	public String index(
			@ParameterObject @ModelAttribute("sampleVO") SampleVO sampleVO, 
			 ModelMap model) throws Exception {
		return this.selectSampleList(sampleVO, model);
	}
}

```

```java

@Schema(description = "샘플 게시물 데이터 모델 (VO)")
public class SampleVO extends SampleDefaultVO {

	private static final long serialVersionUID = 1L;

	/** 아이디 */
	@Schema(description = "게시물 아이디", example = "SAMPLE-00001")
	private String id;

	/** 이름 */
	@EgovNullCheck(message="{confirm.required.name}")
	@Schema(description = "게시물 제목(이름)", example = "Swagger 연동 테스트", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

```

---

## 6. 마무리 및 기대 효과

* **표준화된 개발 체계 구축**: eGovFrame의 안정적인 기반 위에 최신 REST 아키텍처를 결합하여 시스템의 확장성을 확보합니다.
* **유지보수성 향상**: 명확하게 정리된 API 문서 덕분에 프로젝트 인수인계 및 코드 파악이 매우 수월해집니다.
* **성공적인 SI 프로젝트 완수**: 기획, 프론트엔드, 백엔드 간의 불필요한 스펙 확인 과정을 줄여 본연의 비즈니스 로직 개발에 집중할 수 있습니다.

---
#### 참고 자료

* [전자정부프레임워크 포털](https://www.egovframe.go.kr/home/main.do)
* [Swagger 적용방법 (스프링 레거시)](https://keartt.tistory.com/entry/%EC%A0%84%EC%9E%90%EC%A0%95%EB%B6%80-Swagger-%EC%A0%81%EC%9A%A9%EB%B0%A9%EB%B2%95%EC%8A%A4%ED%94%84%EB%A7%81-%EB%A0%88%EA%B1%B0%EC%8B%9C#google_vignette)