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

## 2. 공통 개발환경이 약하면 생기는 문제

Docker나 Dev Container처럼 공통 실행 환경이 있으면:

```text
같은 JDK
같은 Maven 실행 방식
같은 테스트 명령
같은 테스트 결과
```

하지만 그런 환경이 없으면 팀원마다 검증 방식이 달라집니다.

```text
개발자 A: ./mvnw test 실행 후 commit
개발자 B: 로컬 mvn으로 실행해 환경 차이 발생
개발자 C: IDE에서 일부 테스트만 실행
개발자 D: 테스트를 실행하지 않고 commit
   ↓
commit은 모두 가능
   ↓
PR / CI에서 뒤늦게 문제 발견
```

이 발표의 목표는 Docker를 대체하는 것이 아닙니다.

> 공통 개발환경이 부족하더라도  
> commit 전에는 최소한 같은 명령인 `./mvnw test`를 강제하자는 것입니다.

---

## 3. 전체 흐름 먼저 보기

```text
개발자 commit
   ↓
Git pre-commit Hook
   ↓
Husky가 .husky/pre-commit 실행
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

## 4. Git Hook이란?

Git Hook은 Git 명령이 실행되는 특정 시점에 자동으로 실행되는 스크립트입니다.

이번 예제에서 사용하는 Hook은 `pre-commit`입니다.

```text
git commit
   ↓
pre-commit hook 실행
   ↓
성공하면 commit 진행
실패하면 commit 중단
```

즉 `pre-commit`은 commit이 실제로 만들어지기 직전에 실행되는 품질 게이트입니다.

---

## 5. Hook은 어떻게 commit을 막나?

Hook에서 중요한 것은 **종료 코드**입니다.

| 종료 코드 | 의미 |
|---|---|
| `0` | 성공, commit 진행 |
| `1` 이상 | 실패, commit 차단 |

`pre-commit` 안에서 `./mvnw test`를 실행하면:

```text
테스트 성공 -> Maven 종료 코드 0 -> commit 진행
테스트 실패 -> Maven 종료 코드 1 이상 -> commit 중단
```

그래서 테스트 실패를 commit 전에 막을 수 있습니다.

---

## 6. Husky란?

Git Hook은 원래 `.git/hooks`에 직접 만들 수 있습니다.

하지만 `.git/hooks`는 로컬 저장소 내부 설정이라 팀원에게 공유하기 어렵습니다.

Husky는 Hook을 프로젝트 파일로 관리하게 해줍니다.

```text
.husky/pre-commit
package.json
```

정리하면:

- Git Hook: 언제 검사할지 결정
- Maven: 무엇을 검사할지 결정
- Husky: Hook 규칙을 팀 프로젝트 파일로 관리

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

## 8. 커밋 전에 무엇을 실행할 것인가?

| 항목 | `test` | `verify` |
|---|---|---|
| 목적 | 단위 테스트 중심 | 더 넓은 검증 |
| 속도 | 비교적 빠름 | 더 무거울 수 있음 |
| 추천 위치 | `pre-commit` | `pre-push` 또는 CI |

제가 조사한 기준으로는:

- `pre-commit`: `./mvnw test`
- `pre-push` 또는 CI: `./mvnw verify`

---

## 9. 왜 `mvn`이 아니라 `mvnw`인가?

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

## 10. 가장 기본적인 설정 흐름

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

## 11. 기본형 Hook 예시

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

## 12. 이 프로젝트의 Husky 설정

`package.json`:

```json
{
  "private": true,
  "scripts": {
    "prepare": "cd .. && husky git-hook-husky-pre-commit-maven-test/.husky"
  },
  "devDependencies": {
    "husky": "^9.0.0"
  }
}
```

- 일반적인 단일 프로젝트라면 `"prepare": "husky"`
- 이 예제는 하위 폴더 프로젝트라 Husky 경로를 직접 지정

---

## 13. 이 프로젝트의 pre-commit Hook

`.husky/pre-commit`:

```sh
#!/usr/bin/env sh
set -e

echo "[Husky] Commit 전 Maven 테스트를 실행합니다."
cd git-hook-husky-pre-commit-maven-test
./mvnw test
```

동작 흐름:

1. commit 직전에 Hook 실행
2. 예제 프로젝트 폴더로 이동
3. Maven Wrapper로 테스트 실행
4. 테스트 성공 시 commit 진행
5. 테스트 실패 시 commit 중단

---

## 14. 시연 시나리오

서비스 코드:

```java
public void validateName(String name) {
    if (name == null || name.length() < 2) {
        throw new IllegalArgumentException("이름은 2자 이상이어야 합니다.");
    }
}
```

테스트 코드:

```java
assertThrows(IllegalArgumentException.class,
    () -> memberService.validateName("A"));
```

- 의도적으로 조건을 잘못 바꾸면 테스트가 실패
- Hook이 그 실패를 commit 단계에서 차단

핵심:

> 실패한 테스트를 들고 commit하지 못하게 만드는 것

---

## 15. 실패 시연 방법

1. 정상 테스트 확인

```powershell
cd C:\Users\SSAFY\Desktop\egov\egov-vscode2026\git-hook-husky-pre-commit-maven-test
.\mvnw.cmd test
```

예상 결과:

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

2. 서비스 코드를 일부러 잘못 수정

```java
if (name == null || name.length() < 1) {
    throw new IllegalArgumentException("이름은 2자 이상이어야 합니다.");
}
```

3. Git 저장소 루트에서 commit 시도

```powershell
cd C:\Users\SSAFY\Desktop\egov\egov-vscode2026
git add git-hook-husky-pre-commit-maven-test/src/main/java/com/example/huskydemo/member/MemberValidationService.java
git commit -m "test: pre-commit hook demo"
```

4. 테스트 실패로 commit 차단 확인

```text
[Husky] Commit 전 Maven 테스트를 실행합니다.
BUILD FAILURE
```

---

## 16. 성공 시연 방법

서비스 코드를 다시 정상으로 되돌립니다.

```java
if (name == null || name.length() < 2) {
    throw new IllegalArgumentException("이름은 2자 이상이어야 합니다.");
}
```

다시 테스트를 실행합니다.

```powershell
cd C:\Users\SSAFY\Desktop\egov\egov-vscode2026\git-hook-husky-pre-commit-maven-test
.\mvnw.cmd test
```

성공 후 다시 commit을 시도합니다.

```powershell
cd C:\Users\SSAFY\Desktop\egov\egov-vscode2026
git add git-hook-husky-pre-commit-maven-test/src/main/java/com/example/huskydemo/member/MemberValidationService.java
git commit -m "test: pre-commit hook demo"
```

이번에는 테스트가 통과하므로 commit이 진행됩니다.

핵심:

> 같은 commit 명령이라도 테스트 성공 여부에 따라 commit 진행/차단이 결정됩니다.

---

## 17. 한계와 트러블슈팅

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

## 18. 그래서 제 추천안은

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
