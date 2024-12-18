package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointMinMax;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.validator.ParameterValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ParameterValidator parameterValidator;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable, ParameterValidator parameterValidator) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.parameterValidator = parameterValidator;
    }

    /**
     * 포인트 충전
     * @param id 유저 ID
     * @param amount 충전 금액
     * @param updateMillis 타임스탬프
     * @return 충전 후 포인트 정보
     */
    public UserPoint chargeUserPoints(long id, long amount, long updateMillis) throws Exception {
        // 0. 요청 id, 포인트가 음수인지 확인
        parameterValidator.validateId(id);
        parameterValidator.validateAmount(amount);

        // 1. 유저 포인트 조회 및 초기화
        UserPoint currentUserPointInfo = userPointTable.selectById(id);
        long currentPoint = currentUserPointInfo.point();

        // 2. 충전 요청 포인트가 최소 포인트 이상인지 확인
        if (amount < PointMinMax.MIN.getPoint()) {
            throw new Exception("충전 포인트는 " + PointMinMax.MIN.getPoint() + "포인트 이상이여야 합니다.");
        }

        // 3. 포인트 충전 시 최대 잔고를 넘는지 확인
        long chargedPoint = currentPoint + amount;
        if (chargedPoint > PointMinMax.MAX.getPoint()) {
            throw new Exception("충전 후 포인트가" + PointMinMax.MAX.getPoint() + "를 포인트를 초과할 수 없습니다.");
        }

        // 4. 포인트 충전
        UserPoint chargedUserPointInfo = userPointTable.insertOrUpdate(id, chargedPoint);

        // 5. 충전 히스토리 저장
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updateMillis);

        // 6. 업데이트된 포인트 반환
        return chargedUserPointInfo;
    }
    /**
     * 포인트 사용
     * @param id 유저 ID
     * @param amount 사용 금액
     * @param updateMillis 타임스탬프
     * @return 사용 후 포인트 정보
     */
    public UserPoint useUserPoints(long id, long amount, long updateMillis) throws Exception {
        // 0. 요청 id, 포인트가 음수인지 확인
        parameterValidator.validateId(id);
        parameterValidator.validateAmount(amount);

        // 1. 유저 포인트 조회
        UserPoint currentUserPointInfo = userPointTable.selectById(id);
        long currentPoint = currentUserPointInfo.point();

        // 2. 사용 요청 포인트가 최소 포인트 이상인지 확인
        if (amount < PointMinMax.MIN.getPoint()) {
            throw new Exception("사용 포인트는 " + PointMinMax.MIN.getPoint() + "포인트 이상이여야 합니다.");
        }

        // 3. 포인트 사용 금액이 현재 잔고를 넘는지 확인
        if (currentPoint < amount) {
            throw new Exception("현재 보유한 포인트는" + currentPoint + "포인트 입니다. 보유 포인트보다 많은 포인트를 사용할 수 없습니다.");
        }

        // 4. 포인트 사용
        long usedPoint = currentPoint - amount;
        UserPoint UsedUserPointInfo = userPointTable.insertOrUpdate(id, usedPoint);

        // 55. 사용 히스토리 저장
        pointHistoryTable.insert(id, amount, TransactionType.USE, updateMillis);

        // 6. 업데이트된 포인트 반환
        return UsedUserPointInfo;
    }

    /**
     * User Point 조회
     * @param id 유저 ID
     * @return 유저 포인트 정보
     */
    public UserPoint getUserPoint(long id) {
        // 0. 요청 id가 음수인지 확인
        parameterValidator.validateId(id);

        // 1. 유저 포인트 반환
        return userPointTable.selectById(id);
    }
    /**
     * User Point History 조회
     * @param id 유저 ID
     * @return 유저 포인트 이력 리스트
     */
    public List<PointHistory> getUserPointHistoryList(long id) {
        // 0. 요청 id가 음수인지 확인
        parameterValidator.validateId(id);

        // 1. 유저 포인트 반환
        return pointHistoryTable.selectAllByUserId(id);
    }
}
