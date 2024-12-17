package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;

public interface PointService {
    /**
     * 포인트 충전
     * @param id 유저 ID
     * @param amount 충전 금액
     * @param updateMillis 타임스탬프
     * @return 충전 후 포인트 정보
     */
    UserPoint chargeUserPoints(long id, long amount, long updateMillis) throws Exception;

    /**
     * 포인트 사용
     * @param id 유저 ID
     * @param amount 사용 금액
     * @param updateMillis 타임스탬프
     * @return 사용 후 포인트 정보
     */
    UserPoint useUserPoints(long id, long amount, long updateMillis) throws Exception;
}
