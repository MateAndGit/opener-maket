package mateandgit.opener_maket.repsoitory;

import mateandgit.opener_maket.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
