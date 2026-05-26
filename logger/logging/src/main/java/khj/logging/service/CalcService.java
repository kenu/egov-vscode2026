package khj.logging.service;

import khj.logging.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
public class CalcService {
    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int mul(int a, int b) {
        return a * b;
    }

    public int div(int a, int b) throws CustomException {
        if (b == 0) {
            throw new CustomException("b cannot be zero");
        }
        return a / b;
    }

    public int mod(int a, int b) {
        return a % b;
    }
}
