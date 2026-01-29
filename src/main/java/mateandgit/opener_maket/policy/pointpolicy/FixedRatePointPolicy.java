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
        // 1. If 0, validation passes (considered unused)
        if (usePoint.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // 2. Prevent negative amounts less than 0
        if (usePoint.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Point usage cannot be negative");
        }

        // 3. Validate minimum usage unit (e.g., 1 or more)
        if (usePoint.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Minimum point usage is 1 point");
        }

        // 4. Validate balance
        if (usePoint.compareTo(userTotalPoint) > 0) {
            throw new IllegalArgumentException("Insufficient point balance");
        }
    }
}
