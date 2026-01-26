package mateandgit.opener_maket.policy.commissionpolicy;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedRateCommissionPolicy implements CommissionPolicy {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    @Override
    public BigDecimal policy(BigDecimal price) {
        return price.multiply(COMMISSION_RATE)
                .setScale(0, RoundingMode.HALF_UP);
    }
}
