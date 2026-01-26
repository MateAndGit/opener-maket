package mateandgit.opener_maket.policy.pointpolicy;

import java.math.BigDecimal;

public interface PointPolicy {

    BigDecimal calculateRewardPoints(BigDecimal totalPaymentAmount);

    void validatePointUsage(BigDecimal usePoint, BigDecimal userTotalPoint);
}
