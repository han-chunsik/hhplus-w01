package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.PointServiceImpl;
import io.hhplus.tdd.point.validator.ParameterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private ParameterValidator parameterValidator;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private long invalidId;
    private long validId;
    private long invalidAmount;
    private long validAmount;

    @BeforeEach
    void setUp() {
        invalidId = -1L;
        invalidAmount = -100L;
        validId = 10L;
        validAmount = 100L;
    }

    @Nested
    @DisplayName("chargeUserPoints")
    class chargePoint {

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

            @Test
            @DisplayName("충전 후 포인트가 최대 잔고를 넘으면 충전을 실패한다.| 검증: 기존 포인트 + 충전 포인트가 최대 잔고를 넘을 경우 Exception 반환")
            void chargeUserPointsFail1() {
                // Given
                long chargeAmount = 999999999L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(chargeAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.chargeUserPoints(validId, chargeAmount));
            }
        }

    }
}