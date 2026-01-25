package mateandgit.opener_maket.dto;

import mateandgit.opener_maket.domain.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(

        String email,
        List<OrderItemRequest> orderItems,
        BigDecimal totalPrice

) {
}
