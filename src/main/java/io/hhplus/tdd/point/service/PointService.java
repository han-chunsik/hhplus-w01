package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;

public interface PointService {
    /**
     * 포인트 충전
     * @param id 유저 ID
     * @param amount 충전 금액
     * @return 충전 후 포인트 정보
     */
    UserPoint chargeUserPoints(long id, long amount) throws Exception;
}
