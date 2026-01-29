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
        // 1. 기존 데이터 싹 비우기 (매우 중요!)
        userCouponRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();

        // 2. 쿠폰 정책 저장 (ID가 1로 고정되지 않을 수 있으니 생성된 ID를 받아야 함)
        Coupon coupon = Coupon.builder()
                .name("선착순 100명 쿠폰")
                .type(CouponType.ISSUE_LIMITED)
                .totalQuantity(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        // 3. 유저 300명 개별 생성 및 저장
        for (int i = 0; i < 300; i++) {
            User user = User.builder()
                    .email("test" + i + "@test.com") // 이메일 중복 방지
                    .password("test_password")
                    .build();
            userRepository.save(user); // 각각 세이브
        }
    }

    @Test
    @DisplayName("100개의 재고가 있을 떄 300명이 동시에 신청하면 딱 100명만 성공해야 한다.")
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

        System.out.println("최종 발급된 인원: " + issuedCount);
        assertThat(issuedCount).isEqualTo(100);
    }

    @Test
    @DisplayName("동시 발급 후 비동기로 DB에 100개가 모두 저장되어야 한다")
    void async_db_save_test() throws InterruptedException {
        // DB에서 실제 생성된 ID 가져오기
        Long realCouponId = couponRepository.findAll().get(0).getId();
        List<User> users = userRepository.findAll();

        // Redis 초기화
        redisTemplate.opsForValue().set("coupon:inventory:" + realCouponId, "100");
        redisTemplate.delete("coupon:users:" + realCouponId);
        userCouponRepository.deleteAllInBatch();

        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            // 실제 DB에 있는 유저 ID를 순차적으로 전달
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

        System.out.println("Redis 작업 완료. DB 저장을 위해 3초 대기합니다...");
        Thread.sleep(3000);

        long dbCount = userCouponRepository.count();
        System.out.println("최종 DB에 저장된 쿠폰 수: " + dbCount);

        assertThat(dbCount).isEqualTo(100);
    }
}