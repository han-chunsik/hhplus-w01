package io.hhplus.tdd.point;

import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PointConcurrencyControlTest {

    @Autowired
    private PointService pointService;

    @Nested
    @DisplayName("동시성 제어 통합 테스트 시나리오")
    class SuccessCase {
        @Test
        @DisplayName("유저가 1000포인트를 보유하고 있을때, 2번의 500 포인트 충전 요청, 1번의 200포인트 사용 요청을 하면, 히스토리 테이블의 순서는 충전, 충전, 사용이되고, 최종 포인트는 1800포인트를 보유하는지 확인")
        public void pointConcurrencyControlSuccess1() throws Exception {
            //given
            int threadCount = 1;
            long userId = 1L;
            long currentAmount = 1000L;
            long chargeAmount = 500L;
            long useAcount = 200L;
            long finalPoint = 1800L;

            pointService.chargeUserPoints(userId, currentAmount, System.currentTimeMillis());

            CountDownLatch doneSignal = new CountDownLatch(threadCount);
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            // when
            executorService.execute(() -> {
                try {
                    pointService.chargeUserPoints(userId, chargeAmount, System.currentTimeMillis());
                    pointService.chargeUserPoints(userId, chargeAmount, System.currentTimeMillis());
                    pointService.useUserPoints(userId, useAcount, System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            });
            doneSignal.await();
            executorService.shutdown();

            List<PointHistory> pointHistoryList = pointService.getUserPointHistoryList(userId);

            //then
            for (PointHistory pointHistory : pointHistoryList) {
                switch ((int) pointHistory.id()) {
                    case 1:
                        assertEquals(1, pointHistory.userId());
                        assertEquals(1000, pointHistory.amount());
                        assertEquals(TransactionType.CHARGE, pointHistory.type());
                        break;
                    case 2:
                        assertEquals(1, pointHistory.userId());
                        assertEquals(500, pointHistory.amount());
                        assertEquals(TransactionType.CHARGE, pointHistory.type());
                        break;
                    case 3:
                        assertEquals(1, pointHistory.userId());
                        assertEquals(500, pointHistory.amount());
                        assertEquals(TransactionType.CHARGE, pointHistory.type());
                        break;
                    case 4:
                        assertEquals(1, pointHistory.userId());
                        assertEquals(200, pointHistory.amount());
                        assertEquals(TransactionType.USE, pointHistory.type());
                        break;
                }
            }

            assertEquals(finalPoint, pointService.getUserPoint(userId).point());
        }

        @Test
        @DisplayName("A유저의 500 포인트 충전 요청이 병목 현상으로 오래 걸릴 때, B유저의  150 포인트 사용 요청은 정상적으로 처리되는지 확인")
        public void PointConcurrencyControlSuccess2() throws Exception {
            //given
            int threadCount = 1;
            long aUserId = 1L;
            long bUserId = 2L;
            long chargeAmount = 500L;
            long useAmount = 150L;
            long currentAmount = 1000L;

            pointService.chargeUserPoints(bUserId, currentAmount, System.currentTimeMillis());

            CountDownLatch doneSignal = new CountDownLatch(threadCount);
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            // when
            executorService.execute(() -> {
                try {
                    pointService.chargeUserPoints(aUserId, chargeAmount, System.currentTimeMillis());
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            });

            executorService.execute(() -> {
                try {
                    pointService.chargeUserPoints(bUserId, useAmount, System.currentTimeMillis());
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            });
            doneSignal.await();
            executorService.shutdown();

            List<PointHistory> aUserPointHistoryList = pointService.getUserPointHistoryList(aUserId);
            List<PointHistory> bUserPointHistoryList = pointService.getUserPointHistoryList(bUserId);

            long userAUpdateMillis = 0L;
            long userBUpdateMillis = 0L;

            for (PointHistory aUserPointHistory : aUserPointHistoryList) {
                userAUpdateMillis = aUserPointHistory.updateMillis();
            }

            for (PointHistory bUserPointHistory : bUserPointHistoryList) {
                if (bUserPointHistory.userId() == 2) {
                    userBUpdateMillis = bUserPointHistory.updateMillis();
                    break;
                }

            }

            //then
            assertTrue(userBUpdateMillis < userAUpdateMillis);

        }
    }
}