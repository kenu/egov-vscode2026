package egovframework.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
/* 기존 XML 설정들을 임포트하여 초기 가동을 확인하는 전략 */
@ImportResource(locations = {
    "classpath:/egovframework/spring/context-*.xml",
    "classpath:/egovframework/config/egovframework/springmvc/dispatcher-servlet.xml"
})
public class MigrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(MigrationApplication.class, args);
    }
}
