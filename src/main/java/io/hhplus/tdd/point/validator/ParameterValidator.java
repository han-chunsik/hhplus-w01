package io.hhplus.tdd.point.validator;

import org.springframework.stereotype.Component;

@Component
public class ParameterValidator {
    // id 검증 (음수인지 체크)
    public void validateId(long id) throws IllegalArgumentException {
        if (id < 0) {
            throw new IllegalArgumentException("ID는 음수일 수 없습니다.");
        }
    }

    // amount 검증 (음수인지 체크)
    public void validateAmount(long amount) throws IllegalArgumentException {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount는 음수일 수 없습니다.");
        }
    }
}
