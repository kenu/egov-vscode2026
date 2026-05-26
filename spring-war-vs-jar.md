---
marp: true
theme: default
paginate: true
header: 'WAR vs Spring Boot Executable Jar'
footer: '스프링 부트와 내장 톰캣'
---

# WAR vs Spring Boot Executable Jar

전통적인 WAR 배포 방식부터<br>
스프링 부트 실행 가능 Jar까지

<br>

*발표 시간: 약 7분*

---

## 목차

1. **JAR vs WAR** — 두 포맷의 본질적 차이
2. **전통적 배포 방식** — 톰캣 설치부터 WAR 배포까지
3. **WAR 방식의 단점** — 왜 바뀌어야 했는가
4. **내장 톰캣의 등장** — `main()` 하나로 서버 실행
5. **Fat Jar의 한계**
6. **Spring Boot Executable Jar** — jar 안에 jar
7. **정리**

---

## 1. JAR vs WAR

| 구분 | **JAR** (Java Archive) | **WAR** (Web Application Archive) |
|---|---|---|
| 실행 환경 | JVM 위에서 직접 실행 | WAS(톰캣 등) 위에서만 실행 |
| 실행 방법 | `java -jar app.jar` | WAS에 배포 후 WAS가 로딩 |
| 내부 구조 | 클래스 + 리소스 (단순) | `WEB-INF/` 규약을 따르는 복잡한 구조 |
| jar-in-jar | ❌ 불가 (스펙상 제한) | ✅ `WEB-INF/lib` 에 jar 포함 가능 |

> 💡 핵심: **JAR는 jar 안에 jar를 포함할 수 없다.** 이게 나중에 모든 이야기의 출발점이 됩니다.

---

## 2. 전통적 배포 방식

```
[개발자]                            [운영 서버]
                                    
   코드 작성                        톰캣 설치
      ↓                               ↓
   WAR 빌드  ──────────────→  webapps/ROOT.war
   (gradle war)                      ↓
                                  톰캣 실행
                                  (startup.sh)
```

**3단계 절차:**
1. 서버에 톰캣을 **직접 설치**
2. 애플리케이션을 **WAR로 빌드**
3. `톰캣폴더/webapps/ROOT.war` 로 **복사 배포**

---

## 3. WAR 구조

```
server-0.0.1-SNAPSHOT.war
├── WEB-INF/
│   ├── classes/                    ← 컴파일된 .class
│   │   └── hello/servlet/TestServlet.class
│   ├── lib/                        ← 라이브러리 jar들
│   │   └── jakarta.servlet-api-6.0.0.jar
│   └── web.xml                     ← 배치 설정 (생략 가능)
└── index.html                      ← 정적 리소스
```

- `WEB-INF/` 하위: **자바 코드 + 라이브러리 + 설정**
- `WEB-INF/` 외부: **HTML/CSS 같은 정적 리소스**
- **WAR는 jar를 lib 폴더에 포함할 수 있다** → 라이브러리 의존성 문제 없음

---

## 4. WAR 방식의 단점

🔻 **WAS를 별도로 설치해야 한다**
운영 서버마다 톰캣 설치 → 버전 관리 별도

🔻 **개발 환경 설정이 복잡하다**
IDE에서 WAS 연동 설정 (Smart Tomcat, 유료 IntelliJ 등)

🔻 **배포 과정이 번잡하다**
WAR 빌드 → 서버 전송 → webapps에 복사 → 톰캣 재시작

🔻 **톰캣 버전 변경이 어렵다**
서버에 설치된 톰캣을 다시 깔아야 함

> ❓ "톰캣도 자바로 만들어졌는데... 라이브러리처럼 **포함**시키면 안 될까?"

---

## 5. 내장 톰캣 — main()으로 서버 실행

```java
public static void main(String[] args) throws LifecycleException {
    // 톰캣을 자바 코드로 직접 생성
    Tomcat tomcat = new Tomcat();
    Connector connector = new Connector();
    connector.setPort(8080);
    tomcat.setConnector(connector);

    // 디스패처 서블릿 등록
    Context context = tomcat.addContext("", "/");
    tomcat.addServlet("", "dispatcher", dispatcher);
    context.addServletMappingDecoded("/", "dispatcher");

    tomcat.start();   // ← 서버 실행!
}
```

- `tomcat-embed-core` 라이브러리 의존성만 추가하면 끝
- WAS 별도 설치 ❌, `main()` 하나로 끝
- 그런데... **빌드하면 문제가 생긴다**

---

## 6. 첫 번째 시도 — 일반 Jar의 한계

```bash
$ ./gradlew buildJar
$ java -jar embed-0.0.1-SNAPSHOT.jar

Error: Unable to initialize main class hello.embed.EmbedTomcatSpringMain
Caused by: java.lang.NoClassDefFoundError:
    org/springframework/web/context/WebApplicationContext
```

**왜?** Jar 내부를 열어보면:

```
embed-0.0.1-SNAPSHOT.jar
├── hello/embed/EmbedTomcatSpringMain.class
├── hello/spring/HelloController.class
└── (라이브러리 없음 ❌)
```

> **JAR는 jar 안에 jar를 포함할 수 없다** (1번 슬라이드 복습 🔁)

---

## 7. 두 번째 시도 — Fat Jar (Uber Jar)

**아이디어:** "jar는 못 넣어도, **클래스는** 넣을 수 있잖아?"

```groovy
task buildFatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    from { configurations.runtimeClasspath
            .collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```

- 모든 라이브러리 jar의 **압축을 풀어서** 클래스 파일을 하나의 jar로 합침
- 결과: 10MB 이상의 뚱뚱한 jar
- ✅ 동작은 한다!

---

## 8. Fat Jar의 한계

🔻 **어떤 라이브러리가 들어있는지 추적 불가**
모든 게 .class로 풀려 있어서 의존성 확인이 어려움

🔻 **파일명 중복 시 하나만 살아남음** ⚠️ (치명적)

예: 서블릿 컨테이너 초기화 파일
```
META-INF/services/jakarta.servlet.ServletContainerInitializer
```
- A 라이브러리도 이 파일을 가지고 있음
- B 라이브러리도 이 파일을 가지고 있음
- Fat Jar로 합치면 **둘 중 하나만 살아남음** → 다른 하나는 초기화 실패

> 스프링 MVC + 다른 프레임워크를 같이 쓰면 충돌할 수 있다는 뜻

---

## 9. 세 번째 시도 — Spring Boot Executable Jar

**스프링 부트의 발상:** "Jar 스펙을 우리가 다시 정의하자."

자바 표준은 jar 안에 jar를 못 넣지만,
**스프링 부트는 직접 만든 클래스 로더로 jar 안의 jar를 읽을 수 있게 했다.**

```
boot-0.0.1-SNAPSHOT.jar
├── META-INF/MANIFEST.MF
├── org/springframework/boot/loader/
│   └── JarLauncher.class           ← 스프링 부트가 넣어준 실행기
└── BOOT-INF/
    ├── classes/                    ← 내가 짠 코드
    │   └── hello/boot/BootApplication.class
    └── lib/                        ← 라이브러리 jar (압축 그대로!)
        ├── spring-webmvc-6.0.4.jar
        └── tomcat-embed-core-10.1.5.jar
```

---

## 10. MANIFEST.MF의 비밀

```properties
Main-Class:  org.springframework.boot.loader.JarLauncher   # 1️⃣
Start-Class: hello.boot.BootApplication                    # 2️⃣
Spring-Boot-Classes:    BOOT-INF/classes/
Spring-Boot-Lib:        BOOT-INF/lib/
```

**`Main-Class`** — `java -jar`가 가장 먼저 실행하는 클래스
→ 내 코드(`BootApplication`)가 아니라 **`JarLauncher`** 🤔

**`Start-Class`** — 실제 내 애플리케이션의 진입점
→ `JarLauncher`가 준비를 마친 뒤 호출해줌

### 실행 흐름

```
java -jar boot.jar
   → JarLauncher.main()        (BOOT-INF/lib의 jar들을 클래스패스에 로드)
   → BootApplication.main()    (내 코드 시작)
   → SpringApplication.run()   (스프링 컨테이너 + 내장 톰캣)
```

---

## 11. Fat Jar vs Executable Jar

| 항목 | Fat Jar | **Executable Jar** |
|---|---|---|
| 구조 | 모든 .class 평탄화 | jar-in-jar 보존 |
| 라이브러리 추적 | ❌ 어려움 | ✅ `BOOT-INF/lib/` 에서 확인 |
| 파일명 충돌 | ❌ 덮어쓰기 | ✅ 각 jar가 독립적이라 충돌 없음 |
| 표준 여부 | 관례적 | 스프링 부트 자체 스펙 |
| 클래스 로딩 | 일반 ClassLoader | `JarLauncher` 전용 로더 |

---

## 12. 정리

✅ **WAR 시대** — WAS와 애플리케이션이 분리. 유연하지만 설치·배포가 복잡

✅ **내장 톰캣** — `main()` 하나로 서버 실행. 일반 jar로는 라이브러리 문제

✅ **Fat Jar** — 모든 클래스를 평탄화. 동작은 하지만 파일명 충돌 문제

✅ **Executable Jar** — `JarLauncher` + `BOOT-INF/` 구조로 jar 안에 jar
  → Fat Jar의 모든 단점을 해결하면서 단일 파일 배포 유지

> **결국 우리가 매일 쓰는 `java -jar app.jar` 한 줄에는,**
> **이 모든 진화 과정이 응축되어 있다.**

---

# 감사합니다 🙌


<br>

**참고 자료**
- 김영한, 「스프링 부트 - 핵심 원리와 활용」
- Spring Boot Reference Documentation