package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import mateandgit.opener_maket.commissionpolicy.Commissionpolicy;
import mateandgit.opener_maket.domain.Order;
import mateandgit.opener_maket.domain.OrderItem;
import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.dto.OrderRequest;
import mateandgit.opener_maket.dto.ChargeCashRequest;
import mateandgit.opener_maket.repsoitory.OrderItemRepository;
import mateandgit.opener_maket.repsoitory.OrderRepository;
import mateandgit.opener_maket.repsoitory.SellItemRepository;
import mateandgit.opener_maket.repsoitory.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static mateandgit.opener_maket.domain.status.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellItemRepository sellItemRepository;
    private final Commissionpolicy commissionpolicy;

    public BigDecimal chargeCash(ChargeCashRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.addCash(request.cash());

        return user.getCash();
    }

    public Long createOrder(OrderRequest request) {

        // 유저 찾기
        User buyer = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // OrderItem 리스트 생성 (각 상품별로 하나씩)
        List<OrderItem> orderItems = request.orderItems().stream().map(orderItem -> {
            SellItem sellItem = sellItemRepository.findById(orderItem.sellItemId())
                    .orElseThrow(() -> new IllegalArgumentException("sell item not found"));

            sellItem.removeStock(orderItem.count());

            return OrderItem.createOrderItem(sellItem, orderItem.count());
        }).toList();

        // 최종 결제 금액
        BigDecimal totalSum = orderItems.stream()
                .map(item -> item.getOrderPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.totalPrice().compareTo(totalSum) != 0) {
            throw new IllegalArgumentException("total price is not correct");
        }

        // 주문 생성
        Order order = Order.createOrder(buyer, orderItems);
        orderRepository.save(order);

        buyer.removeCash(totalSum);

        return order.getId();
    }

    @Transactional
    public void confirmOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));

        if (order.getStatus() == ORDER_CONFIRMED) {
            throw new IllegalArgumentException("order is already confirmed");
        }

        order.updateStatus(ORDER_CONFIRMED);

        for (OrderItem item : order.getOrderItems()) {

            User seller = item.getSellItem().getUser();

            BigDecimal itemTotal = item.getOrderPrice().multiply(BigDecimal.valueOf(item.getCount()));
            BigDecimal commission = commissionpolicy.policy(itemTotal);
            BigDecimal finalAmount = itemTotal.subtract(commission);

            seller.addCash(finalAmount);
        }
    }

    public void cancelOrder(Long orderId) {

        // 해당 주문 찾기
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));

        // 1. 상태 검증 (배송 중이거나 이미 취소/확정된 경우 방어)
        if (order.getStatus() == SHIPPING || order.getStatus() == ORDER_CONFIRMED || order.getStatus() == CANCELED) {
            throw new IllegalArgumentException("order is already canceled or confirmed");
        }

        // 주문 상태 변경
        order.updateStatus(CANCELED);

        // 유저에게 돈 돌려주기
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        for (OrderItem orderItem : order.getOrderItems()) {

            orderItem.getSellItem().addStock(orderItem.getCount());

            BigDecimal itemAmount = orderItem.getOrderPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getCount()));
            totalRefundAmount = totalRefundAmount.add(itemAmount);
        }

        User buyer = order.getUser();
        buyer.addCash(totalRefundAmount);

    }
}
