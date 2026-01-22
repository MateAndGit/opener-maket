package mateandgit.opener_maket.domain.status;

import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING,
    ORDER,

    PREPARING,
    SHIPPING,
    COMPLETED,

    CANCEL,
    REFUNDING,
    REFUNDED

}
