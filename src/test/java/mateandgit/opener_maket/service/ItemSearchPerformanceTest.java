package mateandgit.opener_maket.service;

import mateandgit.opener_maket.domain.SellItem;
import mateandgit.opener_maket.repository.SellItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ItemSearchPerformanceTest {

    @Autowired
    private DataSeederService dataSeederService;

    @Autowired
    private SellItemRepository sellItemRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Executed exactly once before all tests
        jdbcTemplate.execute("INSERT INTO category (category_id, category_status) VALUES (1, 'FOOD')");
        jdbcTemplate.execute("INSERT INTO users (user_id, email, password, cash, point) VALUES (1, 'seller@test.com', '1234', 0, 0)");
    }

    @Test
    @DisplayName("Verify that 100,000 records are actually inserted into DB.")
    void verifyBulkInsert() {
        // 1. Check count before execution
        long beforeCount = sellItemRepository.count();
        System.out.println("Total count before insertion: " + beforeCount);

        // 2. Insert 100,000 records
        dataSeederService.bulkInsertData(100000);

        // 3. Check count after execution
        long afterCount = sellItemRepository.count();
        System.out.println("Total count after insertion: " + afterCount);

        // 4. Verification: Ensure exactly 100,000 records were added
        assertThat(afterCount).isEqualTo(beforeCount + 100000);

        // 5. Check sample data (Get one random)
        sellItemRepository.findAll(PageRequest.of(0, 1)).getContent().forEach(item -> {
            System.out.println("Data Sample - Name: " + item.getItem().getName());
            System.out.println("Data Sample - Price: " + item.getPrice());
            System.out.println("Data Sample - Sales: " + item.getTotalSales());
        });
    }

    @Test
    @DisplayName("Performance Measurement: JOIN + LIKE Search (Naive)")
    void searchNaiveTest() {
        String keyword = "product_999";
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("JOIN + LIKE Search");

        List<SellItem> results = sellItemRepository.findByItemNameContaining(keyword);

        stopWatch.stop();
        System.out.println("Count found: " + results.size());
        System.out.println("Elapsed time: " + stopWatch.getTotalTimeMillis() + "ms");
    }

    @Test
    void realPerformanceMeasurement() {
        // 1. Prepare 100,000 records
        dataSeederService.bulkInsertData(100_000);

        StopWatch sw = new StopWatch();
        String keyword = "Product_500000";

        // [CASE 1] Simple LIKE Search
        sw.start("Simple LIKE Search");
        sellItemRepository.findByItemNameContaining(keyword);
        sw.stop();

        // [CASE 2] Search sorted by most purchases (Use denormalized fields)
        sw.start("Sorted by Sales (Optimized)");
        sellItemRepository.findAllByItemNameContainingOrderByTotalSalesDesc(keyword, PageRequest.of(0, 20));
        sw.stop();

        System.out.println(sw.prettyPrint());
    }
}