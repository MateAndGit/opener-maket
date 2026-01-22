package mateandgit.opener_maket.repsoitory;

import mateandgit.opener_maket.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
