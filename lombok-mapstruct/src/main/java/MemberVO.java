import lombok.Builder;
import lombok.Getter;

/**
 * 회원 VO.
 * Lombok 이 컴파일 시점에 getMemberNm() 같은 getter 와 builder 를 만들어 준다.
 */
@Getter
@Builder
public class MemberVO {
    private String memberId;
    private String memberNm;
    private String email;
    private int    age;
}
