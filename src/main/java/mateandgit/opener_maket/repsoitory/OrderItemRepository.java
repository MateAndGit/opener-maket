package mateandgit.opener_maket.repsoitory;

import mateandgit.opener_maket.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
