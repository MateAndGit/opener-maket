package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional
public class DataSeederService {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsertData(int count) {
        // 1. Item 테이블에 10만 개 삽입 (이름 검색용)
        String itemSql = "INSERT INTO item (item_id, name, description, category_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(itemSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, i + 1L);
                ps.setString(2, "Product_" + i); // 검색 키워드
                ps.setString(3, "Description for Product_" + i);
                ps.setLong(4, 1L); // 미리 생성된 카테고리 ID
            }
            @Override
            public int getBatchSize() { return count; }
        });

        // 2. SellItem 테이블에 10만 개 삽입 (정렬 및 필터링용)
        String sellItemSql = "INSERT INTO sell_item (sell_item_id, item_id, user_id, price, stock_quantity, deal_status, average_rating, total_sales) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sellItemSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, i + 1L);
                ps.setLong(2, i + 1L); // Item ID 매칭
                ps.setLong(3, 1L);    // 판매자 ID (미리 생성된 User ID)
                ps.setBigDecimal(4, BigDecimal.valueOf(1000 + (Math.random() * 90000)));
                ps.setInt(5, 100);
                ps.setString(6, "SALE");
                ps.setDouble(7, Math.random() * 5); // 별점
                ps.setInt(8, (int) (Math.random() * 10000)); // 판매량
            }
            @Override
            public int getBatchSize() { return count; }
        });
    }
}
