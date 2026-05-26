## 정세호
# 📦 Java Dependency Conflict Troubleshooting Guide

자바 개발 중 발생하는 의존성 충돌은 **"컴파일은 되는데 실행하면 터지는"** 가장 골치 아픈 문제입니다. 이 문서는 그 진단부터 해결까지의 과정을 정리합니다.

---

## 1. 주요 에러 증상 (Symptoms) 🔍

런타임에 다음 에러가 발생한다면 십중팔구 **버전 충돌**입니다.

* **`java.lang.NoSuchMethodError`**
  * A 라이브러리가 필요한 메서드가 포함된 B-v1을 찾는데, 실제로는 해당 메서드가 없는 B-v2가 로드됨.
* **`java.lang.NoClassDefFoundError`**
  * 컴파일 시점에는 존재하던 클래스가 실행 시점 클래스패스에서 사라짐.
* **`java.lang.ClassCastException`**
  * 서로 다른 경로로 들어온 동일 이름의 클래스가 서로 형변환될 때 발생.

---

## 2. 의존성 진단 (Diagnosis)

가장 먼저 할 일은 전체 의존성 지도를 그려보는 것입니다.

```bash
# -Dverbose 옵션은 생략된(omitted) 항목까지 모두 보여줍니다. 
# 의존성 충돌을 해결하고자 한다면 반드시 해당 옵션을 적용해야 합니다.

# 내용이 매우 길게 출력되므로 텍스트 파일로 저장하여 분석합니다.
mvn dependency:tree -Dverbose > tree.txt
```

---

## 3. 의존성 트리 읽는 법 (Analysis)
* **`+-`** : 이 라이브러리 아래에 하위 의존성이 더 있음을 의미.
* **`\-`** : 내 부모의 마지막 자식임을 의미.
* **`()`** : 최종적으로 생략된 의존성.

```text
[INFO] com.example:complex-dependency-lab:jar:1.0-SNAPSHOT
[INFO] +- org.seleniumhq.selenium:selenium-java:jar:4.8.3:compile
[INFO] |  +- org.seleniumhq.selenium:selenium-api:jar:4.8.3:compile
[INFO] |  +- org.seleniumhq.selenium:selenium-chrome-driver:jar:4.8.3:compile
[INFO] |  |  +- com.google.auto.service:auto-service-annotations:jar:1.0.1:compile
[INFO] |  |  +- com.google.auto.service:auto-service:jar:1.0.1:compile
[INFO] |  |  |  \- com.google.auto:auto-common:jar:1.2:compile
```

---

## 4. Dverbose 핵심 메시지 분석 📝

| 표시 문구 | 의미 |
| :--- | :--- |
| **omitted for conflict** | A 버전과 B 버전 충돌 시, Maven 전략에 따라 우선순위가 낮은 버전이 제외됨. |
| **omitted for duplicate** | 이미 다른 경로로 로드되어 있어 가져오지 않음. (정상/안전) |

### 💡 "어차피 답은 정해져 있는데 왜 굳이 알려줄까?"
* 메이븐은 아주 단순한 규칙으로 승자를 정합니다. (예: "A가 1.0, B가 2.0 원하는데 A가 더 가까우니 1.0 쓸래!")
* 하지만 이 **기계적 선택**이 **실제 호환성**을 보장하지 않습니다. "내가 골랐지만 위험할 수 있으니 개발자가 확인해!"라는 경고성 메시지입니다.

---

## 5. Maven의 우선순위: 가까운 정의 전략 📏

**Nearest Definition:** Maven은 트리의 **깊이(Depth)가 더 얕은 쪽**의 버전을 선택합니다.
1. **Direct:** 내가 직접 `pom.xml`에 적은 버전 (최우선)
2. **Transitive:** 남이(라이브러리가) 끌고 들어온 버전 (차선)

```text
# 예시: 더 얕은 깊이에 있는 1.7.10이 1.7.30을 이겨버리는 위험한 상황
 +- org.slf4j:slf4j-api:jar:1.7.10:compile
 |  |  |  +- (org.slf4j:slf4j-api:jar:1.7.30:compile - omitted for conflict with 1.7.10)
```
> 메이븐은 트리를 그리다 "얕은 곳에 똑같은 게 있네? 더 깊이 안 가고 얕은 걸 쓸게"라고 판단합니다.

---

## 6. 해결 방법 ①: 의존성 제거 (`<exclusions>`) ✂️

특정 라이브러리가 불필요하게 낮은 버전을 끌고 올 때 사용합니다.

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <version>1.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.bad-version</groupId>
            <artifactId>conflict-module</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 7. 해결 방법 ②: 버전 강제 고정 (`<dependencyManagement>`) 📌

트리를 내려가며 수십 개를 `exclude` 하기 귀찮다면, 최상단에 딱 한 줄 적습니다. (노란 줄 80% 해결 전략)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version> 
        </dependency>
    </dependencies>
</dependencyManagement>
```
> **핵심:** 깊이가 100단계든 상관없이, 관리자가 버전을 명시하는 순간 모든 하위 계층의 싸움은 즉시 종료됩니다.

---

## 8. 특이 케이스 및 위험도 분석 🚨

### ⚠️ 하위 호환성 붕괴 (Guava 11 vs 31)
* `(omitted for conflict with 31.1-jre)`
* **위험도: 매우 높음.** Hadoop 내부 코드가 Guava 11의 구형 메서드를 호출하면 `NoSuchMethodError`가 발생합니다. 합의가 필요한 시점입니다.

### 🛡️ 9999.0 버전의 비밀
* `listenablefuture:jar:9999.0-empty-to-avoid-conflict-with-guava`
* Guava 내부 클래스와 Standalone 라이브러리 간의 중복 충돌을 피하기 위해, 구글이 만든 **의도적인 빈 껍데기 파일**입니다. 정상적인 작동 원리입니다.

---

## 9. 실무 팁 (Pro-Tips)

### 👑 관리는 전문가에게: BOM 활용
* 가장 똑똑한 방법은 검증된 버전 세트인 **BOM(Bill of Materials)**을 가져오는 것입니다.
* Spring Boot나 Google Cloud는 수백 개 라이브러리 간의 '충돌 없는 황금 밸런스'를 이미 맞춰두었습니다. 직접 명시하기보다 BOM에 의존하는 것이 가장 안전합니다.

### 🛠️ 기타 팁
* **IDE 적극 활용:** IntelliJ의 `Show Dependencies`, `Dependency Analyzer`로 시각적 확인.
* **Scope 확인:** `compile`, `runtime`, `provided` 범위를 명확히 하여 배포본 관리.

* **캐시 정리:**
```
C:\Users\사용자계정\.m2\repository
~/.m2/repository
```
