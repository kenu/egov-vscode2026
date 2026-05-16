package com.example.minwon;

/**
 * 민원 신청 요청 DTO.
 *
 * 검증은 Service 안에서 직접 처리 (필드 단계 어노테이션은 의존성 늘어나서 생략)
 */
public record MinwonRequest(
        String applicantName,    // 신청자명
        String residentNumber,   // 주민등록번호 (13자리)
        MinwonType type,         // 민원 종류
        String content           // 민원 내용
) {
}
