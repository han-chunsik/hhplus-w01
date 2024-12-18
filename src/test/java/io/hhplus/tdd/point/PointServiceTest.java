package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.PointService;
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
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private ParameterValidator parameterValidator;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private long validId;

    @BeforeEach
    void setUp() {
        validId = 1L;
    }

    @Nested
    @DisplayName("chargeUserPoints")
    class ChargePoint {

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

            @Test
            @DisplayName("기존 1000포인트가 있을때, 99999999포인트를 충전할 경우 충전 후 포인트가 최대 포인트(10,000,000) 초과하여  Exception을 반환한다.")
            void chargeUserPointsFail1() {
                // Given
                long chargeAmount = 99999999L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(chargeAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.chargeUserPoints(validId, chargeAmount, currentTime));
            }

            @Test
            @DisplayName("충전 요청 포인트가 100(최소 충전 포인트) 미만일 경우 Exception을 반환한다.")
            void chargeUserPointsFail2() {
                // Given
                long chargeAmount = 99L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(chargeAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.chargeUserPoints(validId, chargeAmount, currentTime));
            }
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("기존 1000포인트를 가지고 있을때, 500 포인트를 충전하면 성공한다.")
            void chargeUserPointsSuccess1() throws Exception {
                // Given
                long chargeAmount = 500L;
                long existingPoint = 1000L;
                long expectedTotalPoint = chargeAmount + existingPoint;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(chargeAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));
                when(userPointTable.insertOrUpdate(validId, expectedTotalPoint)).thenReturn(new UserPoint(validId, expectedTotalPoint, currentTime));
                when(pointHistoryTable.insert(validId, chargeAmount, TransactionType.CHARGE, currentTime)).thenReturn(new PointHistory(1L, validId, chargeAmount, TransactionType.CHARGE, currentTime));

                // When
                UserPoint result = pointService.chargeUserPoints(validId, chargeAmount, currentTime);

                // Then
                assertNotNull(result);
                assertEquals(validId, result.id());
                assertEquals(expectedTotalPoint, result.point());
            }
        }
    }
    @Nested
    @DisplayName("usedUserPoints")
    class UsePoint {

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

            @Test
            @DisplayName("기존 1000포인트가 있을때, 1000포인트를 초과하여 사용할 경우  Exception을 반환한다.")
            void useUserPointsFail1() {
                // Given
                long useAmount = 1001L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(useAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.useUserPoints(validId, useAmount, currentTime));
            }

            @Test
            @DisplayName("사용 요청 포인트가 100(최소 사용 포인트) 미만일 경우 Exception을 반환한다.")
            void useUserPointsFail2() {
                // Given
                long useAmount = 99L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(useAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.useUserPoints(validId, useAmount, currentTime));
            }
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("기존 1000포인트를 가지고 있을때, 500 포인트를 사용하면 성공한다.")
            void useUserPointsSuccess1() throws Exception {
                // Given
                long useAmount = 500L;
                long existingPoint = 1000L;
                long expectedTotalPoint = existingPoint - useAmount;
                long currentTime = System.currentTimeMillis();

                doNothing().when(parameterValidator).validateId(validId);
                doNothing().when(parameterValidator).validateAmount(useAmount);

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));
                when(userPointTable.insertOrUpdate(validId, expectedTotalPoint)).thenReturn(new UserPoint(validId, expectedTotalPoint, currentTime));
                when(pointHistoryTable.insert(validId, useAmount, TransactionType.USE, currentTime)).thenReturn(new PointHistory(1L, validId, useAmount, TransactionType.USE, currentTime));

                // When
                UserPoint result = pointService.useUserPoints(validId, useAmount, currentTime);

                System.out.println(result);

                // Then
                assertNotNull(result);
                assertEquals(validId, result.id());
                assertEquals(expectedTotalPoint, result.point());
            }
        }
    }
}

