package mateandgit.opener_maket.commissionpolicy;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class Commissionpolicy implements Policy{

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    @Override
    public BigDecimal policy(BigDecimal price) {
        return price.multiply(COMMISSION_RATE)
                .setScale(0, RoundingMode.HALF_UP);
    }
}
