package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import mateandgit.opener_maket.domain.Order;
import mateandgit.opener_maket.domain.OrderItem;
import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.dto.ChargeCashRequest;
import mateandgit.opener_maket.dto.OrderRequest;
import mateandgit.opener_maket.policy.commissionpolicy.CommissionPolicy;
import mateandgit.opener_maket.policy.pointpolicy.PointPolicy;
import mateandgit.opener_maket.repository.OrderRepository;
import mateandgit.opener_maket.repository.SellItemRepository;
import mateandgit.opener_maket.repository.UserRepository;
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
    private final SellItemRepository sellItemRepository;
    private final CommissionPolicy commissionPolicy;
    private final PointPolicy pointPolicy;

    /**
     * Charge Cash
     */
    public BigDecimal chargeCash(ChargeCashRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.depositCash(request.cash());

        return user.getCash();
    }

    /**
     * Create Order (Includes point usage and accumulation)
     */
    public Long createOrder(OrderRequest request) {
        User buyer = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // 1. Deduct stock and create order items (Concurrency control with Pessimistic Lock)
        List<OrderItem> orderItems = request.orderItems().stream().map(dto -> {
            SellItem sellItem = sellItemRepository.findByIdWithPessimisticLock(dto.sellItemId())
                    .orElseThrow(() -> new IllegalArgumentException("sell item not found"));

            sellItem.removeStock(dto.count());
            return OrderItem.createOrderItem(sellItem, dto.count());
        }).toList();

        // 2. Calculate total order amount
        BigDecimal totalSum = orderItems.stream()
                .map(item -> item.getOrderPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Process point usage
        BigDecimal usedPoint = request.point();
        if (usedPoint.compareTo(BigDecimal.ZERO) > 0) {
            pointPolicy.validatePointUsage(usedPoint, buyer.getPoint());
            buyer.deductPoint(usedPoint);
        } else {
            usedPoint = BigDecimal.ZERO; // Set to 0 if less than or equal to 0
        }
        // 4. Actual cash payment and point accumulation
        BigDecimal actualCash = totalSum.subtract(usedPoint);
        buyer.withdrawCash(actualCash);

        BigDecimal reward = pointPolicy.calculateRewardPoints(actualCash);
        buyer.earnPoint(reward);

        // 5. Create and save order
        Order order = Order.createOrder(buyer, orderItems, totalSum, usedPoint);
        return orderRepository.save(order).getId();
    }

    /**
     * Confirm Order (Seller Settlement)
     */
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
            BigDecimal commission = commissionPolicy.policy(itemTotal);
            BigDecimal finalAmount = itemTotal.subtract(commission);

            seller.depositCash(finalAmount); // Deposit sales proceeds
        }
    }

    /**
     * Cancel Order (Restore stock and reclaim points)
     */
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));

        // 1. Validate status
        if (order.getStatus() == SHIPPING || order.getStatus() == ORDER_CONFIRMED || order.getStatus() == CANCELED) {
            throw new IllegalArgumentException("order is already canceled or confirmed");
        }

        order.updateStatus(CANCELED);
        User buyer = order.getUser();

        // 2. Restore stock
        order.getOrderItems().forEach(item ->
                item.getSellItem().addStock(item.getCount())
        );

        // 3. Rollback points
        buyer.earnPoint(order.getUsedPoint()); // Restore used points (Earn)

        BigDecimal rewardToRecover = pointPolicy.calculateRewardPoints(order.getActualPaymentAmount());
        buyer.deductPoint(rewardToRecover); // Reclaim earned points (Deduct)

        // 4. Refund cash
        buyer.depositCash(order.getActualPaymentAmount()); // Deposit payment amount
    }
}