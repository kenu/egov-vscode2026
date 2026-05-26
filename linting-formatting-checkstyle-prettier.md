# Linting & Formatting
## Checkstyle과 Prettier로 협업 규칙 자동화하기

OSSCA egov-vscode 2026

---

## 1. 왜 협업 규칙을 자동화해야 할까?

- 여러 사람이 같은 저장소에서 작업하면 코드 스타일 차이가 Pull Request 리뷰 비용으로 이어집니다.
- 들여쓰기, 줄 길이, import 정리 같은 문제는 사람이 반복해서 지적하기보다 도구가 잡는 편이 낫습니다.
- 규칙을 Maven 빌드에 연결하면 로컬, CI, PR 리뷰 기준을 하나로 맞출 수 있습니다.

---

## 2. Linting과 Formatting의 역할

| 구분 | 목적 | 예시 도구 |
|---|---|---|
| Linting | 규칙 위반 탐지 | Checkstyle |
| Formatting | 파일 형식 자동 정렬 | Prettier |

- Checkstyle: Java 코드의 import, 중괄호, 탭 문자, 줄 길이 같은 규칙 위반을 검사합니다.
- Prettier: Markdown, YAML, JSON 같은 문서/설정 파일의 포맷을 일관되게 맞춥니다.

---

## 3. 발표 주제와 예제의 대응

이번 예제는 발표 주제의 각 요소를 다음처럼 연결합니다.

| 발표 키워드 | 예제에서의 구현 |
|---|---|
| Linting | Checkstyle로 Java 코드 규칙 위반 검사 |
| Formatting | Prettier로 Markdown/YAML 포맷 검사 |
| Checkstyle | `checkstyle.xml`에 Java 협업 규칙 정의 |
| Prettier | `.prettierrc.json`, `package.json`에 포맷 규칙과 검사 명령 정의 |
| 협업 규칙 자동화 | Maven `validate` 단계에 두 검사를 연결 |

핵심은 도구를 따로 실행하는 것이 아니라, 팀원이 같은 명령으로 같은 규칙을 검증하게 만드는 것입니다.

---

## 4. Maven 기준 자동화 전략

이번 예제는 `mvn validate` 한 번으로 두 검사를 같이 실행합니다.

```bash
cd linting-formatting-checkstyle-prettier
mvn validate
```

검사 흐름:

1. Maven `validate` 단계 실행
2. Checkstyle로 Java 코드 규칙 검사
3. Prettier로 Markdown/YAML 포맷 검사
4. 위반 사항이 있으면 빌드 실패

---

## 5. 예제 프로젝트 구조

```text
linting-formatting-checkstyle-prettier
├── pom.xml
├── checkstyle.xml
├── package.json
├── .prettierrc.json
├── README.md
└── src
    └── main
        ├── java/com/example/linting
        │   └── CollaborationRules.java
        └── resources/application.yml
```

- Java 코드는 Checkstyle 검사 대상입니다.
- `README.md`, `application.yml`은 Prettier 검사 대상입니다.
- 서버 실행 없이 `mvn validate`만으로 규칙 검사를 보여줄 수 있습니다.

---

## 6. pom.xml 핵심 설정

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <executions>
        <execution>
            <phase>validate</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

- `validate` 단계에 묶으면 `package` 전에 스타일 문제가 먼저 드러납니다.
- PR에서 기능 코드보다 규칙 위반을 먼저 발견할 수 있습니다.

---

## 7. Prettier를 Maven에서 실행하기

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.15.0</version>
</plugin>
```

- Maven 빌드 안에서 Node/NPM을 준비하고 `npm run format:check`를 실행합니다.
- Java 프로젝트라도 문서, YAML, JSON 설정 파일은 Prettier로 관리할 수 있습니다.
- 멘토님 Maven Profile 예제처럼 `src/main/resources`의 설정 파일도 관리 대상에 포함할 수 있습니다.

---

## 8. 3분 시연 순서

1. 정상 빌드 확인

```bash
cd linting-formatting-checkstyle-prettier
mvn validate
```

2. Java 규칙을 일부러 깨고 실패 확인

`CollaborationRules.java`에서 아래 import를:

```java
import java.util.List;
```

다음처럼 바꿉니다.

```java
import java.util.*;
```

다시 실행합니다.

```bash
mvn validate
```

Checkstyle의 `AvoidStarImport` 규칙 때문에 빌드가 실패합니다.

3. 문서/설정 파일 포맷도 검사 대상임을 설명

- `README.md`
- `src/main/resources/application.yml`

위 파일들은 Prettier 검사 대상입니다.

---

## 9. 실무 적용 흐름

- 로컬 개발자: 커밋 전 `mvn validate`로 빠르게 확인합니다.
- PR 작성자: 스타일 수정 커밋과 기능 커밋이 섞이지 않도록 관리합니다.
- 리뷰어: 들여쓰기보다 로직, 설계, 테스트에 집중합니다.
- CI: `mvn validate`를 PR 체크에 연결해 같은 기준으로 검증합니다.

---

## 10. eGovFrame/공공 프로젝트에서의 장점

- 팀원이 바뀌어도 코드 스타일 기준을 유지할 수 있습니다.
- XML, YAML, Markdown 같은 설정/문서 파일까지 일관되게 관리할 수 있습니다.
- Maven 기반 프로젝트와 잘 맞아서 기존 빌드 흐름에 자연스럽게 추가할 수 있습니다.
- 협업 규칙을 문서가 아니라 실행 가능한 빌드 규칙으로 남길 수 있습니다.

---

## 11. 마무리

핵심은 도구 자체가 아니라 팀의 반복 작업을 줄이는 것입니다.

- Java 규칙은 Checkstyle
- 문서와 설정 파일 포맷은 Prettier
- 실행 기준은 Maven `validate`

이렇게 묶으면 로컬과 PR에서 같은 협업 규칙을 자동으로 적용할 수 있습니다.
