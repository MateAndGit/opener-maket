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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static mateandgit.opener_maket.domain.status.CategoryStatus.FOOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
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
    @DisplayName("Accumulated charge adds to existing cash.")
    void chargeCashAccumulation() {
        // when

        SingUpRequest signUp = new SingUpRequest("test@test.com", "password1234");
        Long userId = userService.join(signUp);
        BigDecimal depositAmount = new BigDecimal("10000");
        ChargeCashRequest cashRequest = new ChargeCashRequest("test@test.com", depositAmount);

        BigDecimal depositAmount2 = new BigDecimal("10000");
        ChargeCashRequest cashRequest2 = new ChargeCashRequest("test@test.com", depositAmount2);

        BigDecimal returnedCash = orderService.chargeCash(cashRequest);  // Balance after first charge: 10,000
        BigDecimal returnedCash2 = orderService.chargeCash(cashRequest2); // Balance after second charge: 20,000

        // then
        // 1. Verify return value after first charge
        assertThat(returnedCash).isEqualByComparingTo(new BigDecimal("10000"));

        // 2. Verify return value after second charge (should be 20,000)
        assertThat(returnedCash2).isEqualByComparingTo(new BigDecimal("20000"));

        // 3. Verify final DB state
        User findUser = userRepository.findById(userId).orElseThrow();
        assertThat(findUser.getCash()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    @DisplayName("Creating an order deducts stock and cash, and earns points.")
    void createOrder() {
        // 1. given: Buyer, Seller, Item setup
        SingUpRequest buyerSignUp = new SingUpRequest("buyer@test.com", "password123");
        SingUpRequest sellerSignUp = new SingUpRequest("seller@test.com", "password123");
        Long buyerId = userService.join(buyerSignUp);
        Long sellerId = userService.join(sellerSignUp);

        orderService.chargeCash(new ChargeCashRequest("buyer@test.com", new BigDecimal("50000")));

        Category category = categoryRepository.save(Category.builder().categoryStatus(FOOD).build());

        AddItemRequest itemRequest = new AddItemRequest("seller@test.com", FOOD, "Apple", "Delicious Apple", new BigDecimal("10000"), 10);
        Item item = itemRepository.save(Item.createItem(itemRequest, category));

        User seller = userRepository.findById(sellerId).orElseThrow();

        SellItem apple = sellItemRepository.save(SellItem.createSellItem(itemRequest, seller, item));

        OrderRequest orderRequest = new OrderRequest(
                "buyer@test.com",
                List.of(new OrderItemRequest(apple.getId(), 3)),
                new BigDecimal("30000"), // Total amount calculated by client
                BigDecimal.ZERO          // Points to use
        );

        // 2. when
        Long orderId = orderService.createOrder(orderRequest);

        // 3. then
        // (Verify stock 7, Cash 20,000, Point 750)
        User findBuyer = userRepository.findById(buyerId).orElseThrow();
        assertThat(findBuyer.getCash()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(findBuyer.getPoint()).isEqualByComparingTo(new BigDecimal("750"));
    }

    @Test
    @DisplayName("Ordering with points deducts less cash and earns points based on actual payment amount.")
    void createOrderWithPoints() {
        // 1. Given: Create buyer and charge 50,000
        String email = "pointuser@test.com";
        SingUpRequest buyerSignUp = new SingUpRequest(email, "pass1234");
        Long buyerId = userService.join(buyerSignUp);

        // Charge cash 50,000
        orderService.chargeCash(new ChargeCashRequest(email, new BigDecimal("50000")));

        // 2. Force earn 5,000 points
        // If no service method, get from repository, update and flush
        User buyer = userRepository.findById(buyerId).orElseThrow();
        buyer.earnPoint(new BigDecimal("5000"));
        userRepository.saveAndFlush(buyer); // Reflect in DB immediately

        // 3. Register Item (Apple worth 30,000)
        Category category = categoryRepository.save(Category.builder().categoryStatus(CategoryStatus.FOOD).build());

        AddItemRequest itemReq = new AddItemRequest(
                "seller@test.com",
                CategoryStatus.FOOD,
                "Apple",
                "Delicious Apple",
                new BigDecimal("30000"),
                10
        );

        Item item = itemRepository.save(Item.createItem(itemReq, category));

        // Create arbitrary seller (using buyer as seller for brevity or create separate)
        SellItem apple = sellItemRepository.save(SellItem.createSellItem(itemReq, buyer, item));

        // 4. Create Order Request
        // (email, orderItems, totalPrice, point)
        OrderRequest orderRequest = new OrderRequest(
                email,
                List.of(new OrderItemRequest(apple.getId(), 1)), // 30,000 x 1
                new BigDecimal("30000"),
                new BigDecimal("5000") // Use 5,000 points!
        );

        // 5. When: Execute Order
        orderService.createOrder(orderRequest);

        // 6. Then: Verify Result
        User updatedBuyer = userRepository.findById(buyerId).orElseThrow();

        // (1) Cash verification: 50,000 - (30,000 - 5,000) = 25,000
        assertThat(updatedBuyer.getCash())
                .as("Cash should be deducted less by the amount of points used")
                .isEqualByComparingTo(new BigDecimal("25000"));

        // (2) Point verification: (Existing 5,000 - Used 5,000) + Earned (25,000 * 0.025 = 625)
        assertThat(updatedBuyer.getPoint())
                .as("Points should be accumulated based on actual payment amount after using points")
                .isEqualByComparingTo(new BigDecimal("625"));

        // (3) Stock verification: 10 - 1 = 9
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