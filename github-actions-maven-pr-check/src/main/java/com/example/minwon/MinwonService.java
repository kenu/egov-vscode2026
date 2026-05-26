package com.example.minwon;

/*
 * 민원 신청 처리 서비스
 *
 * 진짜 DB / Spring 빈 / 트랜잭션 같은 부분이 없습니다. 검증 통과하면 접수번호만 만들어서 돌려줍니다.
 *
 */
public class MinwonService {

    public String apply(MinwonRequest req) {
        validate(req);
        return "MIN_" + System.currentTimeMillis();
    }

    private void validate(MinwonRequest req) {
        if (req.applicantName() == null || req.applicantName().length() < 2) {
            throw new EgovBizException("E001", "신청자명은 2자 이상이어야 합니다.");
        }
        if (req.residentNumber() == null || !req.residentNumber().matches("\\d{13}")) {
            throw new EgovBizException("E002", "주민등록번호는 숫자 13자리여야 합니다.");
        }
        if (req.type() == null) {
            throw new EgovBizException("E003", "민원 종류를 선택해주세요.");
        }
        if (req.content() == null || req.content().isBlank()) {
            throw new EgovBizException("E004", "민원 내용을 입력해주세요.");
        }
    }
}
