package egovframework.example.sample.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [예제 3] 실무 사례: 다중 서버 환경에서 Spring Task 의 함정
 *
 * 배경:
 *   - 운영계: WAS 2대 이상으로 이중화
 *   - 두 서버 모두 동일한 @Scheduled 메서드가 떠 있음
 *   - 결과: 정산 배치가 "두 번" 돌아 데이터가 중복 INSERT됨
 *
 * 실제 발생한 증상:
 *   - 일일 정산 결과 금액이 정확히 2배
 *   - SMS 발송 배치에서 동일 고객에게 문자 2통
 *   - DB Unique 제약에 걸려 한 쪽 서버만 ConstraintViolationException
 *
 * Spring Task 만으로 대응할 때의 임시 처방:
 *   ① 환경변수로 "배치 서버" 한 대만 지정 (운영 부담↑)
 *   ② ShedLock 도입 (DB 또는 Redis 기반 분산 락)
 *
 * 근본 해결:
 *   → Quartz + JDBCJobStore 로 전환 (DB 에서 트리거 lock)
 */
@Component
public class MultiServerProblem {

    private static final Logger log = LoggerFactory.getLogger(MultiServerProblem.class);

    // ─────────────────────────────────────────────────────────────
    // [BEFORE] 모든 서버에서 동시에 실행됨 → 중복 처리
    // ─────────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void dailySettlement_problem() {
        log.warn("이 메서드는 클러스터의 모든 노드에서 동시에 실행된다!");
        // settlementService.run();   // ← 중복 실행 위험
    }


    // ─────────────────────────────────────────────────────────────
    // [WORKAROUND] 환경변수로 배치 노드 1대 지정 (간단하지만 SPOF)
    // ─────────────────────────────────────────────────────────────
    @Value("${batch.enabled:false}")
    private boolean batchEnabled;

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void dailySettlement_workaround() {
        if (!batchEnabled) {
            return;   // 지정된 노드 외에는 그냥 건너뜀
        }
        log.info("[Settlement] 배치 노드에서만 실행");
        // settlementService.run();
    }
}


/* ───────────────────────────────────────────────────────────────────
 * [AFTER ① ShedLock - Spring Task 유지하면서 락만 추가]
 * 의존성:
 *   <dependency>
 *     <groupId>net.javacrumbs.shedlock</groupId>
 *     <artifactId>shedlock-spring</artifactId>
 *     <version>5.16.0</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>net.javacrumbs.shedlock</groupId>
 *     <artifactId>shedlock-provider-jdbc-template</artifactId>
 *     <version>5.16.0</version>
 *   </dependency>
 *
 * @SchedulerLock(name = "dailySettlement", lockAtMostFor = "30m", lockAtLeastFor = "1m")
 * @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
 * public void dailySettlement() { ... }
 *
 * → 가장 빠르게 실행을 획득한 노드 1대만 작업을 수행, 나머지는 skip.
 * ─────────────────────────────────────────────────────────────────── */


/* ───────────────────────────────────────────────────────────────────
 * [AFTER ② Quartz JDBCJobStore - 클러스터링 정공법]
 *  - QRTZ_* 테이블에 트리거가 기록되고 DB row-lock 으로 단일 실행 보장
 *  - 노드가 죽어도 다른 노드가 즉시 트리거를 인계받음 (failover)
 *  - 자세한 설정은 04_Quartz_Cluster_Config.xml 참고
 * ─────────────────────────────────────────────────────────────────── */
