package com.example.repository;

import com.example.dto.MemberStatsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    /*
     * [Architecture Note 3-3] DTO 분리 원칙
     * MyBatis 조회 결과는 JPA 엔티티(Entity)가 아닌 화면 요구사항에 맞춘 전용 DTO로 매핑하여, 
     * JPA 도메인 모델과의 구조적 결합도를 낮추는 것이 권장됩니다.
     */
    MemberStatsDto getComplexMemberStats(
            @Param("memberId") Long memberId,
            @Param("isDeleted") Boolean isDeleted,
            @Param("fromDate") String fromDate
    );
}
