package mateandgit.opener_maket.dto;

import java.math.BigDecimal;

public record ChargeCashRequest(
        String email,
        BigDecimal cash
) {
}
