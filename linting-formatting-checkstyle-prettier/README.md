# Linting & Formatting Demo

Maven `validate` 단계에서 Checkstyle과 Prettier를 함께 실행하는 간단한 예제입니다.

## 실행 방법

```bash
mvn validate
```

## 검사 대상

- `src/main/java/**/*.java`: Checkstyle
- `README.md`, `src/main/resources/**/*.yml`: Prettier
