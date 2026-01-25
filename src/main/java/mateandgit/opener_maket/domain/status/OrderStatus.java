package mateandgit.opener_maket.domain.status;

import lombok.Getter;

@Getter
public enum OrderStatus {

    PAYMENT_COMPLETED,
    SHIPPING,
    ORDER_CONFIRMED,
    CANCELED,

}
