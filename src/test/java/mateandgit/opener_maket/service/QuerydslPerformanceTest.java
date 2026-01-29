package mateandgit.opener_maket.service;

import jakarta.transaction.Transaction;
import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.repository.SellItemRepositoryCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class QuerydslPerformanceTest {

    @Autowired
    private SellItemRepositoryCustom sellItemRepositoryCustom;

    @Autowired
    private DataSeederService dataSeederService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("INSERT INTO category (category_id, category_status) VALUES (1, 'FOOD')");
        jdbcTemplate.execute("INSERT INTO users (user_id, email, password, cash, point) VALUES (1, 'seller@test.com', '1234', 0, 0)");
        dataSeederService.bulkInsertData(1_000_000);
    }

    @Test
    @DisplayName("Measure Querydsl dynamic sorting and search performance")
    void searchPerformanceWithQuerydsl() {
        StopWatch sw = new StopWatch();
        String keyword = "Product";

        // 1. Sort by Sales Volume (Use idx_total_sales)
        sw.start("Sorted by Sales (Index)");
        List<SellItem> salesResult = sellItemRepositoryCustom.searchProducts(keyword, "sales");
        sw.stop();

        // 2. Sort by Rating (Use idx_average_rating)
        sw.start("Sorted by Rating (Index)");
        List<SellItem> ratingResult = sellItemRepositoryCustom.searchProducts(keyword, "rating");
        sw.stop();

        // 3. Sort by Newest (Use PK Index)
        sw.start("Sorted by Newest (PK)");
        List<SellItem> newestResult = sellItemRepositoryCustom.searchProducts(keyword, "newest");
        sw.stop();

        System.out.println(sw.prettyPrint());

        // Verify results
        assertThat(salesResult).isNotEmpty();
        System.out.println("Top selling item name: " + salesResult.get(0).getItem().getName());
        System.out.println("Top selling item quantity: " + salesResult.get(0).getTotalSales());
    }
}
