package com.example.minwon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 정상 케이스 1개 + 예외 케이스 3개 = 총 4개

class MinwonServiceTest {

    private final MinwonService service = new MinwonService();

    @Test
    void 정상_민원_신청은_접수번호를_반환한다() {
        MinwonRequest req = new MinwonRequest(
                "정찬영",
                "0202021234567",
                MinwonType.CIVIL_COMPLAINT,
                "도로 보수 요청합니다~"
        );

        String receiptNo = service.apply(req);

        assertNotNull(receiptNo);
        assertTrue(receiptNo.startsWith("MIN_"), "접수번호는 MIN_ 으로 시작해야 함");
    }

    @Test
    void 신청자명이_1자면_E001_예외가_발생한다() {
        MinwonRequest req = new MinwonRequest(
                "정",
                "0202021234567",
                MinwonType.CIVIL_COMPLAINT,
                "내용"
        );

        EgovBizException ex = assertThrows(EgovBizException.class, () -> service.apply(req));
        assertEquals("E001", ex.getErrorCode());
    }

    @Test
    void 주민등록번호_형식이_틀리면_E002_예외가_발생한다() {
        MinwonRequest req = new MinwonRequest(
                "정찬영",
                "abc",   // 숫자 13자리가 아님
                MinwonType.CIVIL_COMPLAINT,
                "내용"
        );

        EgovBizException ex = assertThrows(EgovBizException.class, () -> service.apply(req));
        assertEquals("E002", ex.getErrorCode());
    }

    @Test
    void 민원_내용이_비어있으면_E004_예외가_발생한다() {
        MinwonRequest req = new MinwonRequest(
                "정찬영",
                "0202021234567",
                MinwonType.CIVIL_COMPLAINT,
                "   "   // 내용이 공백
        );

        EgovBizException ex = assertThrows(EgovBizException.class, () -> service.apply(req));
        assertEquals("E004", ex.getErrorCode());
    }
}
