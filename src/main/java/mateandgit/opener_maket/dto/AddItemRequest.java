package mateandgit.opener_maket.dto;

import mateandgit.opener_maket.domain.status.CategoryStatus;

public record AddItemRequest(
        String email,
        CategoryStatus category,
        String itemName,
        String description,
        int price,
        int stockQuantity

) {
}
