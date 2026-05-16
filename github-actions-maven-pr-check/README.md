# GitHub Actions Maven PR Check

발표 주제 #13 _"GitHub Actions를 활용한 Maven 빌드 및 자동 PR 체크 시스템 구축"_ 의 예제 코드입니다

## 구성

- `pom.xml` — JUnit5 만 있는 최소 Maven 프로젝트
- `src/main/java/com/example/minwon/` — 민원 신청 도메인 예제
  - `MinwonType` — 민원 종류 enum
  - `MinwonRequest` — 신청 요청 DTO (record)
  - `EgovBizException` — eGov 표준 비즈니스 예외 패턴 흉내
  - `MinwonService` — 검증 + 접수번호 채번
- `src/test/java/com/example/minwon/MinwonServiceTest.java` — 정상 1 + 예외 3 테스트
- `maven-pr-check.yml` — GitHub Actions 워크플로 (참고용 — 자세한 이유는 파일 상단 주석)
- `images/` — fork 에서 찍은 PR 통과/실패 스크린샷

## 로컬에서 빌드 확인

```bash
cd github-actions-maven-pr-check
mvn clean verify
```

→ 콘솔에 'BUILD SUCCESS' 가 뜨면 완료. 테스트가 4건 실행됐다는 로그가 같이 나옵니다.

## CI 시연

`../github-actions-maven-pr-check.md` 발표 자료 8~9페이지의 스크린샷
