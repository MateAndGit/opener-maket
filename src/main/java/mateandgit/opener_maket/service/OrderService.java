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
     * 캐시 충전
     */
    public BigDecimal chargeCash(ChargeCashRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.depositCash(request.cash());

        return user.getCash();
    }

    /**
     * 주문 생성 (포인트 사용 및 적립 포함)
     */
    public Long createOrder(OrderRequest request) {
        User buyer = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // 1. 재고 차감 및 주문 상품 생성 (비관적 락으로 동시성 제어)
        List<OrderItem> orderItems = request.orderItems().stream().map(dto -> {
            SellItem sellItem = sellItemRepository.findByIdWithPessimisticLock(dto.sellItemId())
                    .orElseThrow(() -> new IllegalArgumentException("sell item not found"));

            sellItem.removeStock(dto.count());
            return OrderItem.createOrderItem(sellItem, dto.count());
        }).toList();

        // 2. 전체 주문 금액 계산
        BigDecimal totalSum = orderItems.stream()
                .map(item -> item.getOrderPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 포인트 사용 처리
        BigDecimal usedPoint = request.point();
        if (usedPoint.compareTo(BigDecimal.ZERO) > 0) {
            pointPolicy.validatePointUsage(usedPoint, buyer.getPoint());
            buyer.deductPoint(usedPoint);
        } else {
            usedPoint = BigDecimal.ZERO; // 0원 이하일 경우 확실히 0으로 세팅
        }
        // 4. 실제 현금 결제 및 포인트 적립
        BigDecimal actualCash = totalSum.subtract(usedPoint);
        buyer.withdrawCash(actualCash);

        BigDecimal reward = pointPolicy.calculateRewardPoints(actualCash);
        buyer.earnPoint(reward);

        // 5. 주문 생성 및 저장
        Order order = Order.createOrder(buyer, orderItems, totalSum, usedPoint);
        return orderRepository.save(order).getId();
    }

    /**
     * 주문 확정 (판매자 정산)
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

            seller.depositCash(finalAmount); // 판매 대금 입금
        }
    }

    /**
     * 주문 취소 (재고 복구 및 포인트 회수)
     */
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));

        // 1. 상태 검증
        if (order.getStatus() == SHIPPING || order.getStatus() == ORDER_CONFIRMED || order.getStatus() == CANCELED) {
            throw new IllegalArgumentException("order is already canceled or confirmed");
        }

        order.updateStatus(CANCELED);
        User buyer = order.getUser();

        // 2. 재고 복구
        order.getOrderItems().forEach(item ->
                item.getSellItem().addStock(item.getCount())
        );

        // 3. 포인트 롤백
        buyer.earnPoint(order.getUsedPoint()); // 썼던 포인트 복구(적립)

        BigDecimal rewardToRecover = pointPolicy.calculateRewardPoints(order.getActualPaymentAmount());
        buyer.deductPoint(rewardToRecover); // 적립됐던 포인트 회수(차감)

        // 4. 현금 환불
        buyer.depositCash(order.getActualPaymentAmount()); // 결제액 입금
    }
}