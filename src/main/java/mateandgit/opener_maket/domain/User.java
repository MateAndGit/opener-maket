package mateandgit.opener_maket.domain;

import jakarta.persistence.*;
import lombok.*;
import mateandgit.opener_maket.dto.SingUpRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private BigDecimal cash = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal point = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<SellItem> items = new ArrayList<>();

    public static User createUser(SingUpRequest request) {

        return User.builder()
                .email(request.email())
                .password(request.password())
                .build();
    }

    // --- Cash Management ---
    public void depositCash(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.cash = this.cash.add(amount);
    }

    public void withdrawCash(BigDecimal amount) {
        if (this.cash.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient cash balance");
        }
        this.cash = this.cash.subtract(amount);
    }

    // --- Point Management ---
    public void earnPoint(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;
        this.point = this.point.add(amount);
    }

    public void deductPoint(BigDecimal amount) {
        if (this.point.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient point balance");
        }
        this.point = this.point.subtract(amount);
    }
}
