package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mateandgit.opener_maket.domain.Coupon;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.domain.UserCoupon;
import mateandgit.opener_maket.repository.CouponRepository;
import mateandgit.opener_maket.repository.UserCouponRepository;
import mateandgit.opener_maket.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static mateandgit.opener_maket.domain.status.CouponStatus.READY;

@Slf4j
@Async("couponAsyncExecutor")
@RequiredArgsConstructor
@Service
@Transactional
public class CouponAsyncService {

    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public void saveToDbAsync(Long couponId, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));

            UserCoupon userCoupon = UserCoupon.createUserCoupon(user, coupon, READY);
            userCouponRepository.save(userCoupon);

        } catch (Exception e) {
            log.error("Failed to save coupon issuance to DB for user: {}, coupon: {}", userId, couponId, e);
        }
    }
}
