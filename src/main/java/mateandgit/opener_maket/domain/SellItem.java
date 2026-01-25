package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.domain.status.DealStatus;
import mateandgit.opener_maket.dto.AddItemRequest;
import mateandgit.opener_maket.exception.NotEnoughStockException;

import java.math.BigDecimal;

import static mateandgit.opener_maket.domain.status.DealStatus.PENDING;

@Entity
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

    public static SellItem createSellItem(AddItemRequest request, User user, Item item) {
        return SellItem.builder()
                .user(user)
                .item(item)
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .build();
    }

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) throw  new NotEnoughStockException("need more stock");
        this.stockQuantity = restStock;
    }

    public void setUser(User user) {
        this.user.getItems().add(this);
    }
}
