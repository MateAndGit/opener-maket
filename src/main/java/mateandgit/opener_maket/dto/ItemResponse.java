package mateandgit.opener_maket.dto;

import lombok.Builder;
import mateandgit.opener_maket.domain.SellItem;

@Builder
public record ItemResponse(
        Long sellItemId,
        String itemName,
        String description,
        long price,
        int stockQuantity,
        double averageRating,
        int totalSales,
        String categoryName
) {

    public static ItemResponse from(SellItem sellItem) {
        return ItemResponse.builder()
                .sellItemId(sellItem.getId())
                .itemName(sellItem.getItem().getName())
                .description(sellItem.getItem().getDescription())
                .price(sellItem.getPrice().longValue()) // Handle BigDecimal
                .stockQuantity(sellItem.getStockQuantity())
                .averageRating(sellItem.getAverageRating())
                .totalSales(sellItem.getTotalSales())
                .categoryName(sellItem.getItem().getCategory().getCategoryStatus().name())
                .build();
    }
}
