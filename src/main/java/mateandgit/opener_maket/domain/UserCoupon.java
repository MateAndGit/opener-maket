package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.domain.status.CouponStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    public void use() {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void revoke() {
        this.status = CouponStatus.REVOKED;
    }

    public static UserCoupon createUserCoupon(User user, Coupon coupon, CouponStatus couponStatus) {
        return UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .status(couponStatus)
                .issuedAt(LocalDateTime.now())
                .build();
    }
}
