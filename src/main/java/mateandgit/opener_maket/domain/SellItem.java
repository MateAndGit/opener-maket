package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.domain.status.DealStatus;
import mateandgit.opener_maket.dto.AddItemRequest;
import mateandgit.opener_maket.exception.NotEnoughStockException;

import java.math.BigDecimal;

import static mateandgit.opener_maket.domain.status.DealStatus.SALE;
import static mateandgit.opener_maket.domain.status.DealStatus.SOLD_OUT;

@Entity
//@Table(name = "sell_item", indexes = {
//        @Index(name = "idx_total_sales", columnList = "total_sales")
//})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Getter
public class SellItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sell_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private BigDecimal price;

    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    private DealStatus dealStatus;

    @Builder.Default
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Builder.Default
    @Column(name = "total_rating_count")
    private Integer totalRatingCount = 0;

    @Builder.Default
    @Column(name = "total_sales")
    private Integer totalSales = 0;

    public static SellItem createSellItem(AddItemRequest request, User user, Item item) {
        return SellItem.builder()
                .user(user)
                .item(item)
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .dealStatus(SALE)
                .build();
    }

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            updateStatus(SOLD_OUT);
            throw  new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

    private void updateStatus(DealStatus dealStatus) {
        this.dealStatus = dealStatus;
    }

    public void setUser(User user) {
        this.user.getItems().add(this);
    }

    public void applyNewRating(int newRating) {
        // New Average = (Old Average * Old Count + New Score) / (Old Count + 1)
        double totalScore = (this.averageRating * this.totalRatingCount) + newRating;
        this.totalRatingCount++;
        this.averageRating = totalScore / this.totalRatingCount;
    }
}
