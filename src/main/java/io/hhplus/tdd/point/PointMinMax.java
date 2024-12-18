package io.hhplus.tdd.point;
/**
 * point min max 정보
 * - MIN : 최소값: 100
 * - MAX : 최대값: 10,000,000
 */
public enum PointMinMax {
    MIN(100L),  // 최소값
    MAX(10000000L);  // 최대값

    private final long point;

    PointMinMax(long point) {
        this.point = point;
    }

    public long getPoint() {
        return point;
    }
}
