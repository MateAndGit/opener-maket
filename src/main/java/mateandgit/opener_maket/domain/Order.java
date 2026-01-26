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

    private BigDecimal totalAmount;         // 상품 총 합계 금액
    private BigDecimal usedPoint;           // 이 주문에서 사용한 포인트
    private BigDecimal actualPaymentAmount; // 실제 현금(Cash) 결제 금액

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
                .actualPaymentAmount(totalAmount.subtract(usedPoint)) // (총액 - 포인트)
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