package com.example.service;

import com.example.domain.Member;
import com.example.dto.MemberStatsDto;
import com.example.repository.MemberMapper;
import com.example.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final EntityManager entityManager;

    /**
     * 회원 목록 전체 조회
     * 📌 단일 테이블 기반의 단순 조회는 JPA(Spring Data JPA)를 활용하여 처리합니다.
     */
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /*
     * [Architecture Note 3-1] 트랜잭션 동기화
     * Spring Boot 환경에서는 JpaTransactionManager가 기본으로 구성되며, 
     * MyBatis는 Spring의 트랜잭션 관리 인프라에 자동으로 참여하여 동일한 영속성/트랜잭션 컨텍스트를 공유합니다.
     */
    @Transactional
    public MemberStatsDto updateMemberAndGetStats(Long memberId, String newName) {
        // 📌 식별자를 통한 단순 엔티티 조회 (JPA 활용)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 📌 JPA 변경 감지(Dirty Checking): 엔티티 상태 변경 시 쓰기 지연 저장소에 UPDATE 쿼리가 적재됩니다.
        member.changeName(newName);
        
        // 📌 영속성 컨텍스트 수동 동기화: MyBatis는 JPA의 1차 캐시를 알지 못하므로, MyBatis 쿼리 실행 직전에 반드시 수동 Flush를 호출하여 DB와 동기화해야 합니다.
        entityManager.flush();

        // 📌 다중 테이블 조인 및 집계 연산이 포함된 복잡한 통계 쿼리는 MyBatis를 활용합니다.
        return memberMapper.getComplexMemberStats(memberId, null, null);
    }

    @Transactional
    public MemberStatsDto softDeleteMember(Long memberId) {
        /*
         * [Architecture Note 3-2] 벌크 연산과 영속성 컨텍스트
         * @Modifying(clearAutomatically = true, flushAutomatically = true) 옵션에 의해
         * JPA 1차 캐시가 자동으로 비워지고 변경사항이 즉각 동기화됩니다.
         * 따라서 직후에 MyBatis 쿼리를 호출하더라도 수동 Flush 작업이 필요하지 않습니다.
         */
        int updatedCount = memberRepository.softDeleteById(memberId, LocalDateTime.now());
        if (updatedCount == 0) {
            throw new IllegalArgumentException("사용자 없음");
        }

        return memberMapper.getComplexMemberStats(memberId, null, null);
    }
}
