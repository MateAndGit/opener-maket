package mateandgit.opener_maket.dto;

import mateandgit.opener_maket.domain.status.CategoryStatus;

import java.math.BigDecimal;

public record AddItemRequest(
        String email,
        CategoryStatus category,
        String itemName,
        String description,
        BigDecimal price,
        int stockQuantity

) {
}
