package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.PointServiceImpl;
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
            @DisplayName("기존포인트 + 충전 포인트가 최대 포인트를 넘을 경우 실패")
            void chargeUserPointsFail1() {
                // Given
                long chargeAmount = 999999999L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.chargeUserPoints(validId, chargeAmount, currentTime));
            }

            @Test
            @DisplayName("충전 포인트가 0(최소 충전 포인트) 이하일 경우 실패")
            void chargeUserPointsFail2() {
                // Given
                long chargeAmount = 99L;
                long existingPoint = 1000L;
                long currentTime = System.currentTimeMillis();

                when(userPointTable.selectById(validId)).thenReturn(new UserPoint(validId, existingPoint, currentTime));

                // When & Then
                assertThrows(Exception.class, () -> pointService.chargeUserPoints(validId, chargeAmount, currentTime));
            }
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유저 ID와 충전 금액을 받아 포인트 충전")
            void chargeUserPointsSuccess1() throws Exception {
                // Given
                long chargeAmount = 500L;
                long existingPoint = 1000L;
                long expectedTotalPoint = chargeAmount + existingPoint;
                long currentTime = System.currentTimeMillis();

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
}