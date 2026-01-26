package mateandgit.opener_maket.repository;

import jakarta.persistence.LockModeType;
import mateandgit.opener_maket.domain.Item;
import mateandgit.opener_maket.domain.SellItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellItemRepository extends JpaRepository<SellItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s from SellItem s where s.id = :id")
    Optional<SellItem> findByIdWithPessimisticLock(@Param("id") Long id);
}
