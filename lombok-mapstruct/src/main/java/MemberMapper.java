import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct 매퍼.
 * 컴파일 시점에 MemberMapperImpl 구현체가 자동 생성된다.
 * 그 구현체는 vo.getMemberNm() 같은 Lombok 산물을 호출하는 코드를 짠다.
 *  → 그래서 Lombok 이 먼저 끝나 있어야 한다.
 */
@Mapper
public interface MemberMapper {

    @Mapping(source = "memberNm", target = "name")
    MemberDto toDto(MemberVO vo);
}
