package mateandgit.opener_maket.service;

import mateandgit.opener_maket.domain.Coupon;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.domain.status.CouponType;
import mateandgit.opener_maket.repository.CouponRepository;
import mateandgit.opener_maket.repository.UserCouponRepository;
import mateandgit.opener_maket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCouponRepository userCouponRepository;
    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        // 1. Clear existing data (Very important!)
        userCouponRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();

        // 2. Save coupon policy (Since ID might not be fixed to 1, retrieve the generated ID)
        Coupon coupon = Coupon.builder()
                .name("First-come, first-served 100 coupons")
                .type(CouponType.ISSUE_LIMITED)
                .totalQuantity(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        // 3. Create and save 300 users individually
        for (int i = 0; i < 300; i++) {
            User user = User.builder()
                    .email("test" + i + "@test.com") // Prevent email duplication
                    .password("test_password")
                    .build();
            userRepository.save(user); // Save each user
        }
    }

    @Test
    @DisplayName("When 300 users apply simultaneously for 100 coupons, exactly 100 should succeed.")
    void concurrency_test() throws InterruptedException {
        // given
        Long couponId = 1L;
        String inventoryKey = "coupon:inventory:" + couponId;
        String userSetKey = "coupon:users:" + couponId;

        redisTemplate.opsForValue().set(inventoryKey, "100");
        redisTemplate.delete(userSetKey);

        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = (long) i;
            executorService.submit(()-> {
                try {
                    couponService.issueCoupon(couponId, userId);;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Thread.sleep(2000);

        // then
        Long issuedCount = redisTemplate.opsForSet().size(userSetKey);

        System.out.println("Total issued count: " + issuedCount);
        assertThat(issuedCount).isEqualTo(100);
    }

    @Test
    @DisplayName("All 100 coupons should be saved to DB asynchronously after concurrent issuance")
    void async_db_save_test() throws InterruptedException {
        // Retrieve the actual ID generated in DB
        Long realCouponId = couponRepository.findAll().get(0).getId();
        List<User> users = userRepository.findAll();

        // Initialize Redis
        redisTemplate.opsForValue().set("coupon:inventory:" + realCouponId, "100");
        redisTemplate.delete("coupon:users:" + realCouponId);
        userCouponRepository.deleteAllInBatch();

        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            // Sequentially pass real user IDs from DB
            Long realUserId = users.get(i).getId();
            executorService.submit(() -> {
                try {
                    couponService.issueCoupon(realCouponId, realUserId);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        System.out.println("Redis operation completed. Waiting 3 seconds for DB save...");
        Thread.sleep(3000);

        long dbCount = userCouponRepository.count();
        System.out.println("Final count saved in DB: " + dbCount);

        assertThat(dbCount).isEqualTo(100);
    }
}