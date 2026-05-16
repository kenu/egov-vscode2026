package com.example.minwon;

/**
 * 비즈니스 예외 클래스
 *
 * 패턴:
 *   - RuntimeException 상속 (롤백 트리거)
 *   - errorCode 를 별도 필드로 보관
 *
 * errorCode 컨벤션: "E" + 3자리 숫자
 */
public class EgovBizException extends RuntimeException {

    private final String errorCode;

    public EgovBizException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
