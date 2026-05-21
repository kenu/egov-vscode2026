package egovframework.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
/* 기존 XML 설정들을 임포트하여 초기 가동을 확인하는 전략 */
@ImportResource(locations = {
    "classpath:/egovframework/spring/context-*.xml",
    "classpath:/egovframework/config/egovframework/springmvc/dispatcher-servlet.xml"
})
/* MyBatis 매퍼 스캔 설정 추가 */
@MapperScan(basePackages = "egovframework.example.sample.service.impl")
public class MigrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(MigrationApplication.class, args);
    }
}
