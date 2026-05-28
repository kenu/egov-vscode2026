package com.example.springbatchegov.batch;

import com.example.springbatchegov.domain.User;
import com.example.springbatchegov.domain.UserCsvDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {

    @Bean
    public Job userMigrationJob(JobRepository jobRepository, Step userMigrationStep) {
        return new JobBuilder("userMigrationJob", jobRepository)
                .start(userMigrationStep)
                .build();
    }

    @Bean
    public Step userMigrationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<UserCsvDto> csvReader,
            ItemProcessor<UserCsvDto, User> processor,
            ItemWriter<User> writer) {

        return new StepBuilder("userMigrationStep", jobRepository)
                .<UserCsvDto, User>chunk(5, transactionManager) // 데모: 5건 단위 (실제 운영: 1000~5000)
                .reader(csvReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public FlatFileItemReader<UserCsvDto> csvReader() {
        return new FlatFileItemReaderBuilder<UserCsvDto>()
                .name("userCsvReader")
                .resource(new ClassPathResource("sample-users.csv"))
                .delimited()
                .names("id", "name", "email", "department")
                .targetType(UserCsvDto.class)
                .linesToSkip(1) // 헤더 스킵
                .build();
    }

    @Bean
    public UserItemWriter userItemWriter(DataSource dataSource) {
        return new UserItemWriter(dataSource);
    }
}
