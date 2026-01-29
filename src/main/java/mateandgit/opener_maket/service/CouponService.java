package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mateandgit.opener_maket.domain.Coupon;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.domain.UserCoupon;
import mateandgit.opener_maket.exception.CouponException;
import mateandgit.opener_maket.repository.CouponRepository;
import mateandgit.opener_maket.repository.UserCouponRepository;
import mateandgit.opener_maket.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static mateandgit.opener_maket.domain.status.CouponStatus.READY;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> issueScript;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CouponAsyncService couponAsyncService;

    public void issueCoupon(Long couponId, Long userId) {
        String inventoryKey = "coupon:inventory:" + couponId;
        String userSetKey = "coupon:users:" + couponId;

        // Execute Redis Lua script (Ensure atomicity)
        Long result = redisTemplate.execute(
                issueScript,
                List.of(inventoryKey, userSetKey),
                userId.toString()
        );

        // Handle result
        handleRedisResult(result);

        // [Success] Save to DB asynchronously to improve user response time
        log.info("Redis issuance successful. Starting DB save request - User: {}, Coupon: {}", userId, couponId);
        couponAsyncService.saveToDbAsync(couponId, userId);
    }

    private void handleRedisResult(Long result) {
        if (result == null) throw new RuntimeException("Redis response error");
        if (result == -1) throw new RuntimeException("Coupon already issued.");
        if (result == -2) throw new RuntimeException("Coupon issuance has ended (Sold out).");
    }

}
