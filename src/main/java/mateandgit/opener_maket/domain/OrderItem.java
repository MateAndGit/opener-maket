package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_item_id")
    private SellItem sellItem;

    private BigDecimal orderPrice;

    private int count;

    public void assignOrder(Order order) {
        this.order = order;
    }

    public static OrderItem createOrderItem(SellItem sellItem, int count) {
        return OrderItem.builder()
                .sellItem(sellItem)
                .orderPrice(sellItem.getPrice())
                .count(count)
                .build();
    }
}
