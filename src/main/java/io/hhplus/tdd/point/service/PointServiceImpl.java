package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointMinMaxType;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    @Override
    public UserPoint chargeUserPoints(long id, long amount, long updateMillis) throws Exception {
        // 1. 유저 포인트 조회 및 초기화
        long currentPoint = getCurrentUserPoints(id);

        // 2. 충전 요청 포인트가 최소 포인트 이상인지 확인
        if (amount < PointMinMaxType.MIN_POINT) {
            throw new Exception("충전 포인트는 " + PointMinMaxType.MAX_POINT + "포인트 이상이여야 합니다.");
        }

        // 3. 포인트 충전 시 최대 잔고를 넘는지 확인
        if (currentPoint + amount > PointMinMaxType.MAX_POINT) {
            throw new Exception("충전 후 포인트가" + PointMinMaxType.MAX_POINT + "를 포인트를 초과할 수 없습니다.");
        }

        // 4. 포인트 충전
        UserPoint chargedUserPointInfo = updateUserPoints(id, currentPoint, amount);

        // 5. 충전 히스토리 저장
        savePointHistory(id, amount, TransactionType.CHARGE, updateMillis);

        // 6. 업데이트된 포인트 반환
        return chargedUserPointInfo;
    }

    @Override
    public UserPoint useUserPoints(long id, long amount, long updateMillis) throws Exception {
        // 1. 유저 포인트 조회
        long currentPoint = getCurrentUserPoints(id);

        // 2. 포인트 사용 금액이 현재 잔고를 넘는지 확인
        if (currentPoint < amount) {
            throw new Exception("현재 보유한 포인트는" + currentPoint + "포인트 입니다. 보유 포인트 이상 사용 불가합니다.");
        }

        // 3. 포인트 사용
        UserPoint usedUserPointInfo = updateUserPoints(id, currentPoint, amount);

        // 4. 사용 히스토리 저장
        savePointHistory(id, amount, TransactionType.USE, updateMillis);

        // 5. 업데이트된 포인트 반환
        return usedUserPointInfo;
    }

    private long getCurrentUserPoints(long id) throws Exception {
        try {
            UserPoint currentUserPointInfo = userPointTable.selectById(id);
            return currentUserPointInfo.point();
        } catch (Exception e) {
            throw new Exception("사용자 포인트 조회 중 오류가 발생했습니다.", e);
        }
    }

    private UserPoint updateUserPoints(long id, long currentPoint, long amount) throws Exception {
        long chargedPoint = currentPoint + amount;
        try {
            return userPointTable.insertOrUpdate(id, chargedPoint);
        } catch (Exception e) {
            throw new Exception("사용자 포인트 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    private void savePointHistory(long id, long amount, TransactionType type, long updateMillis) throws Exception {
        try {
            pointHistoryTable.insert(id, amount, type, updateMillis);
        } catch (Exception e) {
            throw new Exception("히스토리 저장 중 오류가 발생했습니다.", e);
        }
    }
}