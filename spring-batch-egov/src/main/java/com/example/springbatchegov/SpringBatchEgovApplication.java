package com.example.springbatchegov;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBatchEgovApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchEgovApplication.class, args);
    }

    @Bean
    public CommandLineRunner runBatch(JobLauncher jobLauncher, Job userMigrationJob) {
        return args -> {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis()) // 매번 새 JobInstance 생성
                    .toJobParameters();

            System.out.println("=== 배치 시작 ===");
            var result = jobLauncher.run(userMigrationJob, params);
            System.out.println("=== 배치 완료: " + result.getStatus() + " ===");
        };
    }
}
