package egovframework.example;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
	
	static {
        // Model, ModelMap, BindingResult 등 MVC 관련 객체들을 Swagger 문서화 대상에서 제외
        SpringDocUtils.getConfig().addAnnotationsToIgnore(
            org.springframework.ui.Model.class, 
            org.springframework.ui.ModelMap.class, 
            org.springframework.validation.BindingResult.class,
            jakarta.servlet.http.HttpServletRequest.class,
            jakarta.servlet.http.HttpServletResponse.class
        );
    }
		  
	      @Bean
	      OpenAPI openAPI() {
	          return new OpenAPI()
	                  .info(new Info()
	                  .title("전자정부프레임워크 API 명세서")
	                  .description("전자정부프레임워크 샘플 페이지를 위한 REST API 문서입니다.")
	                  .version("v1.0.0"));
	      }

}
