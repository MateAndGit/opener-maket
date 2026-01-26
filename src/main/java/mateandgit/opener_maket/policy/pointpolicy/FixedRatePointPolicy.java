package mateandgit.opener_maket.policy.pointpolicy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedRatePointPolicy implements PointPolicy{

    private static final BigDecimal POINT_RATE = new BigDecimal("0.025"); // 2.5%

    @Override
    public BigDecimal calculateRewardPoints(BigDecimal totalPaymentAmount) {
        return totalPaymentAmount.multiply(POINT_RATE).setScale(0, RoundingMode.FLOOR);
    }

    @Override
    public void validatePointUsage(BigDecimal usePoint, BigDecimal userTotalPoint) {
        // 1. 0원이면 검증 통과 (사용하지 않는 것으로 간주)
        if (usePoint.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // 2. 0보다 작은 마이너스 금액 방지
        if (usePoint.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Point usage cannot be negative");
        }

        // 3. 최소 사용 단위 검증 (예: 1원 이상)
        if (usePoint.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Minimum point usage is 1 point");
        }

        // 4. 잔액 검증
        if (usePoint.compareTo(userTotalPoint) > 0) {
            throw new IllegalArgumentException("Insufficient point balance");
        }
    }
}
