package mateandgit.opener_maket.repository;

import mateandgit.opener_maket.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
