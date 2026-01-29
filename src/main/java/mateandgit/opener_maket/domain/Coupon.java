package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.domain.status.CouponType;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private int totalQuantity;

    private int currentUsageCount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean isStackable;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
}
