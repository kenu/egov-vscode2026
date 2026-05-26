package com.example.controller;

import com.example.dto.MemberStatsDto;
import com.example.domain.Member;
import com.example.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 목록 전체 조회 API (JPA 활용)
     */
    @GetMapping("/members")
    public List<Member> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        return members;
    }

    /**
     * 회원 이름 수정 및 통계 조회 API (JPA 변경 감지 + MyBatis 혼용)
     */
    @PatchMapping("/members/{memberId}/name")
    public MemberStatsDto updateMemberName(
            @PathVariable Long memberId,
            @RequestParam String newName) {
        MemberStatsDto stats = memberService.updateMemberAndGetStats(memberId, newName);
        return stats;
    }

    /**
     * 회원 탈퇴(Soft Delete) 처리 및 통계 조회 API (JPA 벌크 연산 + MyBatis 혼용)
     */
    @DeleteMapping("/members/{memberId}")
    public MemberStatsDto softDeleteMember(@PathVariable Long memberId) {
        MemberStatsDto dto = memberService.softDeleteMember(memberId);
        return dto;
    }
}
