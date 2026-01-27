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
        // 모든 테스트 실행 전에 딱 한 번씩 실행됨
        jdbcTemplate.execute("INSERT INTO category (category_id, category_status) VALUES (1, 'FOOD')");
        jdbcTemplate.execute("INSERT INTO users (user_id, email, password, cash, point) VALUES (1, 'seller@test.com', '1234', 0, 0)");
    }

    @Test
    @DisplayName("데이터 10만 개가 실제로 DB에 들어갔는지 검증한다.")
    void verifyBulkInsert() {
        // 1. 실행 전 개수 확인
        long beforeCount = sellItemRepository.count();
        System.out.println("삽입 전 총 개수: " + beforeCount);

        // 2. 10만 개 데이터 삽입
        dataSeederService.bulkInsertData(100000);

        // 3. 실행 후 개수 확인
        long afterCount = sellItemRepository.count();
        System.out.println("삽입 후 총 개수: " + afterCount);

        // 4. 검증: 정확히 10만 개가 늘었는지 확인
        assertThat(afterCount).isEqualTo(beforeCount + 100000);

        // 5. 샘플 데이터 확인 (무작위 하나 가져오기)
        sellItemRepository.findAll(PageRequest.of(0, 1)).getContent().forEach(item -> {
            System.out.println("데이터 샘플 - 이름: " + item.getItem().getName());
            System.out.println("데이터 샘플 - 가격: " + item.getPrice());
            System.out.println("데이터 샘플 - 판매량: " + item.getTotalSales());
        });
    }

    @Test
    @DisplayName("성능 측정: JOIN + LIKE 검색(Naive)")
    void searchNaiveTest() {
        String keyword = "product_999";
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("JOIN + LIKE Search");

        List<SellItem> results = sellItemRepository.findByItemNameContaining(keyword);

        stopWatch.stop();
        System.out.println("검색된 개수: " + results.size());
        System.out.println("소요 시간: " + stopWatch.getTotalTimeMillis() + "ms");
    }

    @Test
    void realPerformanceMeasurement() {
        // 1. 데이터 10만 개 준비
        dataSeederService.bulkInsertData(100_000);

        StopWatch sw = new StopWatch();
        String keyword = "Product_500000";

        // [CASE 1] 단순 LIKE 검색
        sw.start("Simple LIKE Search");
        sellItemRepository.findByItemNameContaining(keyword);
        sw.stop();

        // [CASE 2] 구매 많은 순 정렬 검색 (비정규화 필드 활용)
        sw.start("Sorted by Sales (Optimized)");
        sellItemRepository.findAllByItemNameContainingOrderByTotalSalesDesc(keyword, PageRequest.of(0, 20));
        sw.stop();

        System.out.println(sw.prettyPrint());
    }
}