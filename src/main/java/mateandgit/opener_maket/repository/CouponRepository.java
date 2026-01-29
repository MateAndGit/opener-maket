package mateandgit.opener_maket.repository;

import mateandgit.opener_maket.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
