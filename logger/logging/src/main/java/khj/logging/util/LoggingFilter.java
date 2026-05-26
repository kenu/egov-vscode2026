package khj.logging.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            // 요청을 CachedBodyHttpServletRequest로 래핑
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);

            // URL, 메서드 및 요청 바디 로깅
            String url = wrappedRequest.getRequestURI();
            String method = wrappedRequest.getMethod();
            String body = wrappedRequest.getReader().lines().reduce("", String::concat);

            log.info("Incoming Request: URL={}, Method={}, Body={}", url, method, body);

            // 래핑된 요청 객체를 다음 필터 체인으로 전달
            chain.doFilter(wrappedRequest, response);
        } else {
            // HttpServletRequest가 아닌 경우 그대로 전달
            chain.doFilter(request, response);
        }
    }
}
