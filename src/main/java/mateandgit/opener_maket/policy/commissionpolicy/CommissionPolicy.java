package mateandgit.opener_maket.policy.commissionpolicy;

import java.math.BigDecimal;

public interface CommissionPolicy {

    BigDecimal policy(BigDecimal price);

}
