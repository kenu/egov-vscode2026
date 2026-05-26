package khj.logging.controller;

import khj.logging.exception.CustomException;
import khj.logging.service.CalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoggingController {
    @Autowired
    private CalcService calcService;

    @GetMapping("/logs/debug")
    public ResponseEntity<String> debug() {
//        log.debug("debug");
        return ResponseEntity.ok("debug");
    }

    @GetMapping("/calculations/sum/{a}/{b}")
    public ResponseEntity<Integer> sum(@PathVariable int a, @PathVariable int b) {
        int result = calcService.add(a, b);
//        log.info("sum result: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calculations/difference/{a}/{b}")
    public ResponseEntity<Integer> difference(@PathVariable int a, @PathVariable int b) {
        int result = calcService.sub(a, b);
//        log.info("difference result: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calculations/product/{a}/{b}")
    public ResponseEntity<Integer> product(@PathVariable int a, @PathVariable int b) {
        int result = calcService.mul(a, b);
//        log.info("product result: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calculations/quotient/{a}/{b}")
    public ResponseEntity<?> quotient(@PathVariable int a, @PathVariable int b) {
        try {
            int result = calcService.div(a, b);
//            log.info("quotient result: {}", result);
            return ResponseEntity.ok(result);
        } catch (CustomException e) {
            log.error("quotient error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("b cannot be zero");
        }
    }

    @GetMapping("/calculations/remainder/{a}/{b}")
    public ResponseEntity<Integer> remainder(@PathVariable int a, @PathVariable int b) {
        int result = calcService.mod(a, b);
//        log.info("remainder result: {}", result);
        return ResponseEntity.ok(result);
    }
}
