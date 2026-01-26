package mateandgit.opener_maket.repository;

import mateandgit.opener_maket.domain.Category;
import mateandgit.opener_maket.domain.status.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryStatus(CategoryStatus category);
}
