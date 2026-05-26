package egovframework.example.sample.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [예제 1] Spring Task - 가장 단순한 스케줄러
 *
 * 사용 시점: 단일 서버, 고정 주기, 단순 배치
 * 장점: @Scheduled 한 줄로 끝
 * 한계: 동적 변경/클러스터링/이력관리 불가
 *
 * 활성화 조건:
 *   - @EnableScheduling 이 설정 클래스에 선언되어 있어야 함
 *   - 전자정부프레임워크 부트 템플릿: EgovBootApplication 에 보통 선언되어 있음
 */
@Component
public class DailyReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyReportScheduler.class);

    /**
     * 매일 새벽 2시 일일 리포트 생성
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void generateDailyReport() {
        log.info("[DailyReport] 일일 리포트 생성 시작");
        try {
            // 비즈니스 로직
            Thread.sleep(500); // 작업 시뮬레이션
            log.info("[DailyReport] 일일 리포트 생성 완료");
        } catch (Exception e) {
            // 주의: @Scheduled 메서드의 예외는 호출자에게 전파되지 않고 로그만 남음
            // 따라서 반드시 try-catch 로 직접 처리해야 한다.
            log.error("[DailyReport] 리포트 생성 실패", e);
        }
    }

    /**
     * fixedDelay: 직전 실행이 끝난 시점 기준 N ms 후 재실행
     * fixedRate : 직전 실행이 시작된 시점 기준 N ms 후 재실행 (작업이 길면 겹칠 수 있음)
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    public void healthCheck() {
        log.debug("[HealthCheck] ping");
    }
}
