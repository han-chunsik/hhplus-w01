package io.hhplus.tdd.point;

import io.hhplus.tdd.point.validator.ParameterValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParameterValidatorTest {

    @InjectMocks
    private final ParameterValidator parameterValidator = new ParameterValidator();

    @Nested
    @DisplayName("validateId")
    class ValidateId {

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

            @Test
            @DisplayName("id가 음수일 경우 IllegalArgumentException을 던진다.")
            void validateIdWhenNegative() {
                // Given: 음수 id 값
                long invalidId = -1L;

                // When & Then: validateId가 IllegalArgumentException을 던지는지 확인
                assertThatThrownBy(() -> parameterValidator.validateId(invalidId))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("ID는 음수일 수 없습니다.");
            }
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("id가 정상값일 경우 예외가 발생하지 않는다.")
            void validateIdWhenValid() {
                // Given: 정상 id 값
                long validId = 10L;

                // When & Then: validateId가 예외 없이 정상적으로 실행되는지 확인
                parameterValidator.validateId(validId); // 예외가 발생하지 않아야 함
            }
        }
    }

    @Nested
    @DisplayName("validateAmount")
    class ValidateAmount {

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

            @Test
            @DisplayName("amount가 음수일 경우 IllegalArgumentException을 던진다.")
            void validateAmountWhenNegative() {
                // Given: 음수 amount 값
                long invalidAmount = -1L;

                // When & Then: validateAmount가 IllegalArgumentException을 던지는지 확인
                assertThatThrownBy(() -> parameterValidator.validateAmount(invalidAmount))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Amount는 음수일 수 없습니다.");
            }
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("amount가 정상값일 경우 예외가 발생하지 않는다.")
            void validateAmountWhenValid() {
                // Given: 정상 amount 값
                long validAmount = 10L;

                // When & Then: validateAmount가 예외 없이 정상적으로 실행되는지 확인
                parameterValidator.validateAmount(validAmount); // 예외가 발생하지 않아야 함
            }
        }
    }
}