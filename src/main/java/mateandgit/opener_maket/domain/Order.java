package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.domain.status.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static mateandgit.opener_maket.domain.status.OrderStatus.PAYMENT_COMPLETED;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;         // Total amount of products
    private BigDecimal usedPoint;           // Points used in this order
    private BigDecimal actualPaymentAmount; // Actual cash payment amount

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    public static Order createOrder(User user, List<OrderItem> orderItems, BigDecimal totalAmount, BigDecimal usedPoint) {
        Order order = Order.builder()
                .user(user)
                .status(PAYMENT_COMPLETED)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .usedPoint(usedPoint)
                .actualPaymentAmount(totalAmount.subtract(usedPoint)) // (Total Amount - Points)
                .build();

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        return order;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }
}