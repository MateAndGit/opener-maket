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

        // Redis Lua 스크립트 실행 (원자성 보장)
        Long result = redisTemplate.execute(
                issueScript,
                List.of(inventoryKey, userSetKey),
                userId.toString()
        );

        // 결과 핸들링
        handleRedisResult(result);

        // [성공] 비동기로 DB에 저장하여 사용자 응답 속도 향상
        log.info("Redis 발급 성공. DB 저장 요청 시작 - User: {}, Coupon: {}", userId, couponId);
        couponAsyncService.saveToDbAsync(couponId, userId);
    }

    private void handleRedisResult(Long result) {
        if (result == null) throw new RuntimeException("Redis 응답 에러");
        if (result == -1) throw new RuntimeException("이미 발급받은 쿠폰입니다.");
        if (result == -2) throw new RuntimeException("쿠폰이 선착순 마감되었습니다.");
    }

}
