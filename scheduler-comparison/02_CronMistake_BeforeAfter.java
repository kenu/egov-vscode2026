package egovframework.example.sample.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [예제 2] 실무 사례: Cron 표현식 실수 - Before / After
 *
 * 배경:
 *   "매월 1일 새벽 3시"에 정산 배치를 돌리려고 작성.
 *   Linux crontab 형식(분 시 일 월 요일, 5필드)에 익숙해서 그대로 옮겼다가
 *   Spring/Quartz는 6필드(초 추가)라는 점을 놓침.
 *
 * 에러 로그(실제 발생한 형태):
 *   org.springframework.beans.factory.BeanCreationException:
 *     Encountered invalid @Scheduled method 'monthlySettlement':
 *     Cron expression must consist of 6 fields (found 5 in "0 3 1 * *")
 *
 * 핵심:
 *   - Linux  crontab: 분 시 일 월 요일                (5필드)
 *   - Spring cron   : 초 분 시 일 월 요일             (6필드)
 *   - Quartz cron   : 초 분 시 일 월 요일 [연도]      (6 또는 7필드)
 *   - Quartz는 "일"과 "요일" 중 한 쪽을 반드시 '?'로 둬야 함
 */
@Component
public class SettlementSchedulerExamples {

    private static final Logger log = LoggerFactory.getLogger(SettlementSchedulerExamples.class);

    // ─────────────────────────────────────────────────────────────
    // [BEFORE] 5필드 - 기동 시점에 BeanCreationException 으로 죽음
    // ─────────────────────────────────────────────────────────────
    // @Scheduled(cron = "0 3 1 * *")   // ❌ Linux 형식
    // public void monthlySettlement_BAD() { ... }


    // ─────────────────────────────────────────────────────────────
    // [AFTER] 6필드 + 명시적 timezone
    // ─────────────────────────────────────────────────────────────
    /**
     * 매월 1일 03:00:00 (KST) 정산 배치
     */
    @Scheduled(cron = "0 0 3 1 * *", zone = "Asia/Seoul")  // ✅
    public void monthlySettlement() {
        log.info("[Settlement] 월정산 시작");
        // ...
    }

    // ─────────────────────────────────────────────────────────────
    // 자주 헷갈리는 표현식 비교
    // ─────────────────────────────────────────────────────────────
    // 매 5분마다           : "0 */5 * * * *"     (초가 0인 매 5분)
    // 매 5초마다           : "*/5 * * * * *"
    // 평일 09:00           : "0 0 9 * * MON-FRI"
    // 매시 정각            : "0 0 * * * *"
    // 매일 점심 12시       : "0 0 12 * * *"
    // (Quartz 전용) 매월 1일 03시: "0 0 3 1 * ?"   ← '?' 주의

    /**
     * 흔한 함정: fixedRate 와 긴 작업
     *  - fixedRate=60000 인데 작업이 90초 걸리면, 직전 작업과 새 작업이 겹친다.
     *  - 단일 스레드 풀이면 큐잉, 멀티 스레드면 동시 실행.
     *  - "절대 겹치면 안 되는" 작업은 fixedDelay 또는 분산 락 사용.
     */
    @Scheduled(fixedRate = 60_000)
    public void overlapWarningExample() {
        // 의도치 않은 동시 실행을 피하려면 fixedDelay 로 바꾸거나
        // synchronized / Redis 락 / DB 락을 추가한다.
    }
}
