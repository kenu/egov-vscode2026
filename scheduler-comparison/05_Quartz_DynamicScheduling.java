package egovframework.example.sample.scheduler;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [예제 5] Quartz 동적 스케줄링
 *
 * Spring Task 로는 불가능한 것:
 *   - 운영 중 관리자 화면에서 Cron 식 변경
 *   - 사용자가 등록한 알림 시간에 맞춰 Job 동적 생성
 *   - 실패한 Job 수동 재실행, 일시 정지/재개
 *
 * Quartz Scheduler API 만 있으면 위 모든 것이 가능하다.
 *
 * eGovFrame 부트 템플릿에서는 SchedulerFactoryBean 이 만든
 * 'scheduler' 빈을 그대로 주입받아 사용한다.
 */
@Service
public class DynamicSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulerService.class);

    @Autowired
    private Scheduler scheduler;     // SchedulerFactoryBean 이 등록한 빈

    /**
     * 런타임에 새 Job 등록
     */
    public void registerJob(String jobName, String cronExpr, Class<? extends Job> jobClass)
            throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, "DYNAMIC")
                .storeDurably()
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", "DYNAMIC")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpr)
                        .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul")))
                .forJob(jobDetail)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("[Dynamic] Job 등록 완료: {} / {}", jobName, cronExpr);
    }

    /**
     * Cron 표현식 변경 (Job 은 그대로, Trigger만 교체)
     */
    public void rescheduleJob(String jobName, String newCronExpr) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", "DYNAMIC");

        CronTrigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(newCronExpr))
                .forJob(jobName, "DYNAMIC")
                .build();

        scheduler.rescheduleJob(triggerKey, newTrigger);
        log.info("[Dynamic] Cron 변경: {} → {}", jobName, newCronExpr);
    }

    /** 일시 정지 / 재개 / 삭제 */
    public void pauseJob(String jobName) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobName, "DYNAMIC"));
    }
    public void resumeJob(String jobName) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobName, "DYNAMIC"));
    }
    public void deleteJob(String jobName) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(jobName, "DYNAMIC"));
    }

    /** 즉시 1회 실행 (관리자 화면 "지금 실행" 버튼) */
    public void triggerNow(String jobName) throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey(jobName, "DYNAMIC"));
    }
}
