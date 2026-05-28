---
marp: true
theme: default
paginate: true
---

# Git Hook(Husky)을 활용한 Commit 전 Maven Test 강제화
### 공통 환경이 부족한 프로젝트에서 최소한의 품질 규칙 만들기

OSSCA egov-vscode 2026 / Issue #3

---

## 1. 왜 이 주제인가?

- 이상적: Docker 같은 공통 실행 환경으로 버전과 실행 조건 통일
- 현실: 모든 프로젝트가 그렇게 구성되지는 않음
- 문제: JDK, Maven, PATH, OS 차이로 같은 코드도 실행 결과가 달라질 수 있음
- 결론: 환경을 완전히 통일하지 못하더라도, **커밋 전 테스트 규칙**은 만들 수 있음

> 이번 발표는 "환경 통일" 자체보다  
> "공통 환경이 부족할 때 최소한의 품질 게이트를 만드는 방법"에 집중합니다.

---

## 2. 실제로 어떤 문제가 생기나?

```bash
git add .
git commit -m "fix: 회원가입 검증 수정"
```

- 로컬 커밋은 매우 쉽게 진행됨
- 그런데 `mvn test`를 빼먹으면:
  - PR의 CI에서 늦게 실패
  - 더 나쁘면 merge 이후 main에서 문제 발생
- 즉 테스트는 "하면 좋은 일"이 아니라 "강제되어야 하는 일"에 가까움

---

## 3. 역할을 나눠 보면 더 명확하다

- **Maven/Gradle**
  - 빌드와 테스트를 실제로 실행
- **Git Hook**
  - 특정 Git 시점에 스크립트를 강제 실행
- **Husky**
  - Hook을 프로젝트 규칙으로 관리하기 쉽게 해줌

정리하면:

- Maven은 **무엇을 검사할지**
- Git Hook은 **언제 검사할지**
- Husky는 **그 규칙을 어떻게 공유할지**

---

## 4. 제가 추천하는 기본 구조

```text
개발자 commit
   ↓
Husky pre-commit
   ↓
./mvnw test
   ↓
성공하면 commit 진행 / 실패하면 commit 차단
   ↓
CI에서 ./mvnw verify
```

- 로컬: 빠른 1차 차단
- CI: 더 강한 최종 검증

---

## 5. 커밋 전에 무엇을 실행할 것인가?

| 항목 | `test` | `verify` |
|---|---|---|
| 목적 | 단위 테스트 중심 | 더 넓은 검증 |
| 속도 | 비교적 빠름 | 더 무거울 수 있음 |
| 추천 위치 | `pre-commit` | `pre-push` 또는 CI |

제가 조사한 기준으로는:

- `pre-commit`: `./mvnw test`
- `pre-push` 또는 CI: `./mvnw verify`

---

## 6. 왜 `mvn`이 아니라 `mvnw`인가?

- 개발자마다 Maven 버전이 다를 수 있음
- PATH 설정이 다를 수 있음
- 공통 환경이 없을수록 이 차이가 더 크게 드러남

그래서 추천:

```bash
./mvnw test
```

- 프로젝트가 요구하는 Maven 버전으로 실행
- 로컬 환경 차이를 줄임
- Hook에서도 같은 실행 경로 사용 가능

---

## 7. 왜 `.git/hooks` 대신 Husky인가?

> "그냥 `.git/hooks/pre-commit` 직접 만들면 안 되나?"

가능하지만 Husky가 더 실용적입니다.

1. `.git/hooks`는 로컬 저장소 내부라 버전 관리가 불편함
2. Husky는 `.husky/`를 프로젝트 안에서 관리하게 해줌
3. 팀원이 clone 받아도 같은 규칙을 재현하기 쉬움

즉 Husky의 핵심은 Hook 실행 자체보다  
**Hook을 팀 규칙으로 관리하는 방식**입니다.

---

## 8. 가장 기본적인 설정 흐름

```bash
npm install --save-dev husky
npx husky init
```

```json
{
  "scripts": {
    "prepare": "husky"
  }
}
```

- `.husky/pre-commit` 생성
- `prepare`로 팀원 환경에서도 Hook 초기화

---

## 9. 기본형 예시

```sh
#!/usr/bin/env sh
set -e

echo "[pre-commit] Maven unit test 실행"
./mvnw test
```

- `set -e`: 하나라도 실패하면 즉시 종료
- 테스트 실패 시 commit도 중단

개념 설명용으로는 가장 단순하고 명확한 형태입니다.

---

## 10. 실무형 예시

```sh
#!/usr/bin/env sh
set -e

echo "[Husky] Commit 전 Maven 테스트를 실행합니다."

if ./mvnw test; then
  echo "[Husky] 테스트 성공. commit 진행"
else
  echo "[Husky] 테스트 실패. commit 중단"
  exit 1
fi
```

- 루트형 Spring Boot Maven 프로젝트에서 바로 사용 가능
- 시연할 때도 흐름이 잘 보임

---

## 11. 시연 시나리오

```java
public void validateName(String name) {
    if (name == null || name.length() < 2) {
        throw new IllegalArgumentException("이름은 2자 이상이어야 합니다.");
    }
}
```

```java
assertThrows(IllegalArgumentException.class,
    () -> memberService.validateName("A"));
```

- 의도적으로 조건을 잘못 바꾸면 테스트가 실패
- Hook이 그 실패를 commit 단계에서 차단

핵심:

> 실패한 테스트를 들고 commit하지 못하게 만드는 것

---

## 12. 한계와 트러블슈팅

- Hook이 아예 안 도는 경우
  - `pre-commit` 파일명, `core.hooksPath` 확인
- GUI에서만 `command not found`
  - PATH / Node 초기화 문제 가능
- 커밋이 너무 느림
  - `test`는 로컬, `verify`는 CI로 분리
- `skipTests`, `maven.test.skip=true`
  - Hook이 있어도 품질 게이트가 무력화될 수 있음
- `git commit --no-verify`
  - Hook은 우회 가능

---

## 13. 그래서 제 추천안은

- 공통 환경이 약한 프로젝트일수록 `mvnw`를 우선 사용
- `pre-commit`에는 `./mvnw test`
- 더 무거운 검증은 CI의 `./mvnw verify`
- Husky로 Hook을 프로젝트 규칙으로 관리
- Hook만 믿지 말고 CI + Branch Protection까지 연결

한 줄 요약:

> 테스트를 습관이 아니라 규칙으로 바꾸는 것이 핵심입니다.

---

## 참고 자료

- Maven Lifecycle
  - https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html
- Maven Wrapper
  - https://maven.apache.org/wrapper/index.html
- Maven Surefire Plugin
  - https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html
- Git Hooks
  - https://git-scm.com/docs/githooks
- Husky
  - https://typicode.github.io/husky/
