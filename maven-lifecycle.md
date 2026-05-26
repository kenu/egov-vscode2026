# Maven Lifecycle

## 왜 eGovFrame은 Maven을 많이 사용할까?

### Gradle과 비교하며 보는 빌드 철학의 차이

---

## 1. Lifecycle이란?

Lifecycle은:

```
어떤 작업이 어떤 순서로 진행되는가
```

를 의미합니다.

Maven은 Java 프로젝트 빌드를 다음 흐름으로 강제합니다.

```
compile
→ test
→ package
→ verify
→ install
→ deploy
```

즉 Maven은:

```
Java 프로젝트는 이런 순서로 빌드되어야 한다
```

는 철학을 가지고 있습니다.

---

## 2. 실제 Maven 실행 흐름

```bash
.\mvnw.cmd clean package
```

를 실행하면 실제로는:

```
clean
↓
compile
↓
test
↓
package
```

가 자동 수행됩니다.

즉 package만 실행되는 것이 아니라,

그 이전 단계까지 모두 자동 수행됩니다.

---

## 3. compile / test / package 차이

| 단계 | 의미 |
| --- | --- |
| compile | Java 코드 문법 검사 + `.class` 생성 |
| test | 비즈니스 로직 테스트 |
| package | `.jar` / `.war` 생성 |

---

## 4. Maven 특징

```
test 실패 시 package 단계로 못 간다
```

---

### 성공 케이스

```bash
.\mvnw.cmd clean package
```

↓

```
BUILD SUCCESS
```

↓

```
target/app.jar 생성
```

---

### 실패 케이스

```bash
.\mvnw.cmd clean package -Pfailure-demo
```

↓

```
BUILD FAILURE
```

↓

```
jar 생성 중단
```

Maven 철학:

```
테스트가 통과되지 않은 결과물은 배포하지 말자
```

---

## 5. Maven vs Gradle 차이

보통 최근 Spring Boot 프로젝트는 Gradle을 많이 사용합니다.

Gradle은 build 자체를 프로그래밍처럼 구성할 수 있습니다.

```
if (profile == "prod") {
    implementaion 'postgresql'
} else {
    implementation 'h2'
}
```

- 개발 환경
- 운영 환경

에 따라 동적으로 build 구성이 가능합니다.

반면 Maven은:

```
compile
→ test
→ package
```

같은 공정을 강하게 표준화합니다.

```
개발자가 자유롭게 만드는 것보다
정해진 build 흐름을 따르자
```

는 철학이 강합니다.

| Maven | Gradle |
| --- | --- |
| Lifecycle 중심 | Task 중심 |
| 정해진 공정 흐름 | 프로그래밍 가능한 build |
| 표준화 | 유연성 |
| 정형화 | 커스터마이징 가능 |

---

## 8. 현재는 공공도 많이 바뀌고 있다

예전 eGovFrame 환경은:

```
WAR 생성
→ 외부 Tomcat 배포
```

구조가 많았습니다.

현재는:

```
Git Push
→ Jenkins
→ mvn package
→ Docker build
→ Kubernetes deploy
```

같은 흐름도 점점 증가하고 있습니다.

즉 현재는 공공도:

- Docker
- Kubernetes
- CI/CD

기반 구조로 점점 변화하고 있습니다.