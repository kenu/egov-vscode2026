# Validation 프레임워크 활용: `@Valid`와 `BindingResult` 처리

## 발표 개요

- 대상: Spring MVC 또는 eGovFrame 기반 웹 애플리케이션에서 입력값 검증을 구현하는 개발자
- 목표: `MemberRequest`에 검증 규칙을 선언하고, 컨트롤러에서 `@Valid`와 `BindingResult`로 검증 결과를 처리하는 흐름을 이해한다.

## 1. 왜 입력값 검증이 필요한가

웹 애플리케이션은 사용자의 입력을 그대로 신뢰하면 안 된다. 필수값 누락, 이메일 형식 오류, 길이 초과, 숫자 범위 오류 같은 문제는 서비스 로직에 들어가기 전에 걸러야 한다.

컨트롤러 안에서 `if` 문으로 직접 검증할 수도 있지만, 조건이 늘어날수록 코드가 복잡해지고 재사용하기 어렵다. Spring에서는 Bean Validation을 사용해 요청 DTO에 검증 규칙을 선언할 수 있다.

## 2. `MemberRequest`와 Bean Validation

`MemberRequest`는 회원 등록 요청 데이터를 담는 DTO다. 여기에 `@NotBlank`, `@Size`, `@Email` 같은 검증 어노테이션을 붙이면 입력값 검증 규칙을 DTO 안에 모을 수 있다.

대표적인 검증 어노테이션은 다음과 같다.

- `@NotBlank`: 빈 문자열 또는 공백만 있는 값을 허용하지 않는다.
- `@Size`: 문자열이나 컬렉션의 길이를 제한한다.
- `@Email`: 이메일 형식인지 확인한다.
- `@Min`, `@Max`: 숫자의 최소값과 최대값을 제한한다.

즉, `MemberRequest`는 단순히 값을 전달하는 객체에서 끝나는 것이 아니라, "회원 등록 요청 데이터가 가져야 할 최소한의 규칙"까지 표현하는 역할을 할 수 있다.

## 3. `@Valid`의 역할

`@Valid`는 컨트롤러 메서드의 파라미터에 붙여 해당 DTO의 검증 어노테이션을 실행하라는 의미다.

```java
public String save(@Valid MemberRequest memberRequest, BindingResult bindingResult)
```

위처럼 선언하면 Spring은 요청 값을 `MemberRequest`에 바인딩한 뒤, `MemberRequest`에 선언된 검증 규칙을 자동으로 검사한다.

## 4. `BindingResult`의 역할

`BindingResult`는 검증 결과와 바인딩 오류를 담는 객체다. 검증에 실패했을 때 어떤 필드에서 어떤 오류가 발생했는지 확인할 수 있다.

중요한 규칙은 `BindingResult`를 `@Valid`가 붙은 파라미터 바로 뒤에 선언해야 한다는 점이다.

순서가 떨어져 있으면 Spring이 검증 오류를 `BindingResult`에 담지 못하고 예외 처리 흐름으로 넘어갈 수 있다.

## 5. 처리 흐름 예시

1. 사용자가 입력 화면에서 값을 제출한다.
2. Spring이 요청 파라미터를 `MemberRequest`에 바인딩한다.
3. `@Valid`가 `MemberRequest`의 검증 어노테이션을 실행한다.
4. 오류가 있으면 `BindingResult`에 오류 정보가 저장된다.
5. 컨트롤러에서 `bindingResult.hasErrors()`로 오류 여부를 확인한다.
6. 오류가 있으면 입력 화면으로 돌아가고, 오류가 없으면 비즈니스 로직을 실행한다.

## 6. 짧은 예시 코드

### 요청 DTO

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    public MemberRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

`@ModelAttribute("memberRequest") MemberRequest memberRequest` 형태로 받을 때 Spring은 기본 생성자로 DTO 객체를 만들고, 요청 파라미터를 setter로 바인딩한다. 이 방식은 구버전 eGovFrame 또는 오래된 Spring MVC 환경에서도 호환성이 좋아 발표 예시로 사용했다.

### Controller

```java
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    @GetMapping("/members/new")
    public String create(@ModelAttribute("memberRequest") MemberRequest memberRequest) {
        return "member/input";
    }

    @PostMapping("/members")
    public String save(
            @Valid @ModelAttribute("memberRequest") MemberRequest memberRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "member/input";
        }

        // memberService.save(memberRequest);
        return "redirect:/members";
    }
}
```

## 7. eGovFrame에서는 같은 방식인가?

eGovFrame도 Spring MVC를 기반으로 하기 때문에 `@Valid`와 `BindingResult`를 사용하는 기본 형식은 같다. 컨트롤러에서 요청 DTO 또는 VO 객체 앞에 `@Valid`를 붙이고, 바로 뒤에 `BindingResult`를 선언해 검증 오류를 처리하는 흐름은 동일하다.

다만 프로젝트 버전에 따라 import 패키지가 달라질 수 있다.

- Spring Boot 3 또는 최신 Jakarta 기반: `jakarta.validation.Valid`
- Spring Boot 2 또는 기존 Java EE 기반: `javax.validation.Valid`

eGovFrame 프로젝트에서는 관례상 VO 객체를 많이 사용하므로 `MemberRequest` 대신 `MemberVO`, `SearchVO` 같은 이름을 사용할 수 있다. 이름은 달라도 검증 어노테이션을 객체에 선언하고 컨트롤러에서 `@Valid`와 `BindingResult`를 함께 사용하는 구조는 같다.

특히 구버전 eGovFrame에서는 기본 생성자와 getter/setter가 있는 일반 class 형태의 VO/DTO가 호환성이 좋다. 최신 Spring 기반 프로젝트라면 record DTO도 사용할 수 있지만, 호환성과 일반성을 고려해 예시를 선택했다.

```java
@PostMapping("/members")
public String save(@Valid MemberVO memberVO, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        return "member/input";
    }

    // memberService.insertMember(memberVO);
    return "redirect:/members";
}
```

## 8. 발표 마무리

`MemberRequest`에 검증 규칙을 선언하면 입력값 검증 기준을 한곳에 모을 수 있다. `@Valid`는 그 규칙을 실행하고, `BindingResult`는 검증 실패 결과를 컨트롤러에서 처리할 수 있게 해준다.

실무에서는 단순한 형식 검증은 어노테이션으로 처리하고, 중복 이메일 검사처럼 DB 조회가 필요한 검증은 서비스 계층이나 커스텀 Validator로 분리하는 방식이 좋다.
