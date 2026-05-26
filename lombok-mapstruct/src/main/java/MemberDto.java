import lombok.Builder;
import lombok.Getter;

/**
 * 응답용 DTO.
 * 일부러 필드명을 살짝 바꿨다: memberNm → name.
 */
@Getter
@Builder
public class MemberDto {
    private String memberId;
    private String name;        // ← MemberVO.memberNm 과 다른 이름
    private String email;
    private int    age;
}
