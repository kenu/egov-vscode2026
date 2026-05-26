package com.example.repository;

import com.example.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

    /** 
     * JPA CRUD 전용 리포지토리 
     */
    public interface MemberRepository extends JpaRepository<Member, Long> {

    /*
     * [Architecture Note 3-2] 벌크 수정 연산의 한계와 활용
     * 벌크 수정 쿼리는 JPA의 영속성 컨텍스트를 우회하여 DB에 직접 쿼리를 전송하므로, 
     * Auditing(엔티티 리스너)을 거치지 않아 updatedDate와 같은 메타 데이터 필드를 쿼리에서 직접 갱신해야 합니다.
     * 
     * ※ 주의: 실무에서 단건 레코드 갱신 시에는 영속성 컨텍스트를 안전하게 활용하는 '변경 감지(Dirty Checking)' 방식이 권장되나, 
     * 본 코드는 벌크 연산 직후 MyBatis 연동 테스트(Flush/Clear 옵션 활용)를 위한 학습용 예제로 작성되었습니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.isDeleted = true, m.updatedDate = :updatedDate where m.id = :memberId")
    int softDeleteById(@Param("memberId") Long memberId, @Param("updatedDate") LocalDateTime updatedDate);
}
