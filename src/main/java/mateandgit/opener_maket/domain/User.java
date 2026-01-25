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
    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<SellItem> items = new ArrayList<>();

    public static User createUser(SingUpRequest request) {

        return User.builder()
                .email(request.email())
                .password(request.password())
                .cash(ZERO)
                .build();
    }

    public void addCash(BigDecimal cash) {
        if (cash.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("cash must be greater than zero");
        }
        this.cash = this.cash.add(cash);
    }

    public void removeCash(BigDecimal totalAmount) {
        if (this.cash.compareTo(totalAmount) <= 0) {
            throw new IllegalArgumentException("cash is not enough");
        }
        this.cash = this.cash.subtract(totalAmount);
    }

}
