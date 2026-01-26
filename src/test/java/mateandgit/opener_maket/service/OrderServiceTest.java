package mateandgit.opener_maket.service;

import mateandgit.opener_maket.domain.*;
import mateandgit.opener_maket.domain.status.CategoryStatus;
import mateandgit.opener_maket.dto.*;
import mateandgit.opener_maket.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static mateandgit.opener_maket.domain.status.CategoryStatus.FOOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    SellItemRepository sellItemRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("Should successfully deposit cash to user account")
    void chargeCash() {
        // given
        SingUpRequest signUp = new SingUpRequest("test@test.com", "password1234");
        Long userId = userService.join(signUp);
        BigDecimal depositAmount = new BigDecimal("10000");
        ChargeCashRequest cashRequest = new ChargeCashRequest("test@test.com", depositAmount);

        // when
        BigDecimal returnedCash = orderService.chargeCash(cashRequest);

        // then
        assertThat(returnedCash).isEqualByComparingTo(depositAmount);

        User findUser = userRepository.findById(userId).orElseThrow();
        assertThat(findUser.getCash()).isEqualByComparingTo(depositAmount);
    }

    @Test
    @DisplayName("누적 충전을 하면 기존에 캐시에서 추가로 충전된다.")
    void 누적_충전_캐시() {
        // when

        SingUpRequest signUp = new SingUpRequest("test@test.com", "password1234");
        Long userId = userService.join(signUp);
        BigDecimal depositAmount = new BigDecimal("10000");
        ChargeCashRequest cashRequest = new ChargeCashRequest("test@test.com", depositAmount);

        BigDecimal depositAmount2 = new BigDecimal("10000");
        ChargeCashRequest cashRequest2 = new ChargeCashRequest("test@test.com", depositAmount2);

        BigDecimal returnedCash = orderService.chargeCash(cashRequest);  // 첫 충전 후 잔액: 10,000
        BigDecimal returnedCash2 = orderService.chargeCash(cashRequest2); // 두 번째 충전 후 잔액: 20,000

        // then
        // 1. 첫 번째 충전 후 반환값 확인
        assertThat(returnedCash).isEqualByComparingTo(new BigDecimal("10000"));

        // 2. 두 번째 충전 후 반환값 확인 (여기가 20,000원이어야 함!)
        assertThat(returnedCash2).isEqualByComparingTo(new BigDecimal("20000"));

        // 3. 최종 DB 상태 확인
        User findUser = userRepository.findById(userId).orElseThrow();
        assertThat(findUser.getCash()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    @DisplayName("주문을 생성하면 재고와 캐시가 차감되고 포인트가 적립된다.")
    void createOrder() {
        // 1. given: 구매자, 판매자, 상품 세팅
        SingUpRequest buyerSignUp = new SingUpRequest("buyer@test.com", "password123");
        SingUpRequest sellerSignUp = new SingUpRequest("seller@test.com", "password123");
        Long buyerId = userService.join(buyerSignUp);
        Long sellerId = userService.join(sellerSignUp);

        orderService.chargeCash(new ChargeCashRequest("buyer@test.com", new BigDecimal("50000")));

        Category category = categoryRepository.save(Category.builder().categoryStatus(FOOD).build());

        AddItemRequest itemRequest = new AddItemRequest("seller@test.com", FOOD, "사과", "맛있는 사과", new BigDecimal("10000"), 10);
        Item item = itemRepository.save(Item.createItem(itemRequest, category));

        User seller = userRepository.findById(sellerId).orElseThrow();

        SellItem apple = sellItemRepository.save(SellItem.createSellItem(itemRequest, seller, item));

        OrderRequest orderRequest = new OrderRequest(
                "buyer@test.com",
                List.of(new OrderItemRequest(apple.getId(), 3)),
                new BigDecimal("30000"), // 클라이언트가 계산해서 보낸 총액
                BigDecimal.ZERO          // 사용할 포인트
        );

        // 2. when
        Long orderId = orderService.createOrder(orderRequest);

        // 3. then
        // (재고 7개 확인, 캐시 20,000원 확인, 포인트 750원 확인)
        User findBuyer = userRepository.findById(buyerId).orElseThrow();
        assertThat(findBuyer.getCash()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(findBuyer.getPoint()).isEqualByComparingTo(new BigDecimal("750"));
    }

    @Test
    @DisplayName("포인트를 사용해 주문하면, 사용한 만큼 캐시가 덜 차감되고 실결제액 기준으로 포인트가 적립된다.")
    void createOrderWithPoints() {
        // 1. Given: 구매자 생성 및 5만 원 충전
        String email = "pointuser@test.com";
        SingUpRequest buyerSignUp = new SingUpRequest(email, "pass1234");
        Long buyerId = userService.join(buyerSignUp);

        // 현금 50,000원 충전
        orderService.chargeCash(new ChargeCashRequest(email, new BigDecimal("50000")));

        // 2. 포인트 5,000원 강제 적립
        // 서비스 메서드가 없다면 repository에서 직접 가져와서 업데이트 후 flush
        User buyer = userRepository.findById(buyerId).orElseThrow();
        buyer.earnPoint(new BigDecimal("5000"));
        userRepository.saveAndFlush(buyer); // DB에 즉시 반영

        // 3. 상품 등록 (30,000원짜리 사과)
        Category category = categoryRepository.save(Category.builder().categoryStatus(CategoryStatus.FOOD).build());

        AddItemRequest itemReq = new AddItemRequest(
                "seller@test.com",
                CategoryStatus.FOOD,
                "사과",
                "맛있는 사과",
                new BigDecimal("30000"),
                10
        );

        Item item = itemRepository.save(Item.createItem(itemReq, category));

        // 판매자 임의 생성 (간결함을 위해 buyer를 seller로 활용하거나 별도 생성)
        SellItem apple = sellItemRepository.save(SellItem.createSellItem(itemReq, buyer, item));

        // 4. 주문 요청 생성
        // (email, orderItems, totalPrice, point)
        OrderRequest orderRequest = new OrderRequest(
                email,
                List.of(new OrderItemRequest(apple.getId(), 1)), // 30,000원 1개
                new BigDecimal("30000"),
                new BigDecimal("5000") // 포인트 5,000원 사용!
        );

        // 5. When: 주문 실행
        orderService.createOrder(orderRequest);

        // 6. Then: 결과 검증
        User updatedBuyer = userRepository.findById(buyerId).orElseThrow();

        // (1) 캐시 검증: 50,000 - (30,000 - 5,000) = 25,000
        assertThat(updatedBuyer.getCash())
                .as("사용한 포인트만큼 캐시가 적게 차감되어야 함")
                .isEqualByComparingTo(new BigDecimal("25000"));

        // (2) 포인트 검증: (기존 5,000 - 사용 5,000) + 적립(25,000 * 0.025 = 625)
        assertThat(updatedBuyer.getPoint())
                .as("포인트 사용 후 실결제액 기준 적립금이 합산되어야 함")
                .isEqualByComparingTo(new BigDecimal("625"));

        // (3) 재고 검증: 10 - 1 = 9
        SellItem updatedItem = sellItemRepository.findById(apple.getId()).orElseThrow();
        assertThat(updatedItem.getStockQuantity()).isEqualTo(9);
    }

    @Test
    void confirmOrder() {
    }

    @Test
    void cancelOrder() {
    }
}