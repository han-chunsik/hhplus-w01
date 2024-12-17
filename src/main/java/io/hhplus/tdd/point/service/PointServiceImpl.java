package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.validator.ParameterValidator;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ParameterValidator parameterValidator;

    private static final long MAX_POINT = 10000000L; // 최대 포인트 값

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable, ParameterValidator parameterValidator) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.parameterValidator = parameterValidator;
    }

    @Override
    public UserPoint chargeUserPoints(long id, long amount) throws Exception {
        try {
            // 1. 유효성 검사
            parameterValidator.validateId(id);
            parameterValidator.validateAmount(amount);

            // 2. 유저 포인트 조회 및 초기화
            long currentPoint = getCurrentUserPoints(id);

            // 3. 포인트 충전 시 최대 잔고를 넘는지 확인
            if (currentPoint + amount > MAX_POINT) {
                throw new Exception("충전 후 포인트가 최대값을 초과할 수 없습니다.");
            }

            // 4. 포인트 충전
            UserPoint chargedUserPointInfo = updateUserPoints(id, currentPoint, amount);

            // 5. 충전 히스토리 저장
            savePointHistory(id, amount);

            // 6. 업데이트된 포인트 반환
            return chargedUserPointInfo;
        } catch (Exception e) {
            throw new Exception("사용자 포인트 충전 중 오류가 발생했습니다.", e);
        }
    }

    private long getCurrentUserPoints(long id) {
        UserPoint currentuserPointInfo = userPointTable.selectById(id);
        return (currentuserPointInfo == null) ? 0L : currentuserPointInfo.point();
    }

    private UserPoint updateUserPoints(long id, long currentPoint, long amount) throws Exception {
        long chargedPoint = currentPoint + amount;
        try {
            return userPointTable.insertOrUpdate(id, chargedPoint);
        } catch (Exception e) {
            throw new Exception("사용자 포인트 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    private void savePointHistory(long id, long amount) throws Exception {
        try {
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        } catch (Exception e) {
            throw new Exception("히스토리 저장 중 오류가 발생했습니다.", e);
        }
    }
}