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
        // 1. Insert 100,000 records into Item table (For name search)
        String itemSql = "INSERT INTO item (item_id, name, description, category_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(itemSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, i + 1L);
                ps.setString(2, "Product_" + i); // Search keyword
                ps.setString(3, "Description for Product_" + i);
                ps.setLong(4, 1L); // Pre-created Category ID
            }
            @Override
            public int getBatchSize() { return count; }
        });

        // 2. Insert 100,000 records into SellItem table (For sorting and filtering)
        String sellItemSql = "INSERT INTO sell_item (sell_item_id, item_id, user_id, price, stock_quantity, deal_status, average_rating, total_sales) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sellItemSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, i + 1L);
                ps.setLong(2, i + 1L); // Match Item ID
                ps.setLong(3, 1L);    // Seller ID (Pre-created User ID)
                ps.setBigDecimal(4, BigDecimal.valueOf(1000 + (Math.random() * 90000)));
                ps.setInt(5, 100);
                ps.setString(6, "SALE");
                ps.setDouble(7, Math.random() * 5); // Rating
                ps.setInt(8, (int) (Math.random() * 10000)); // Sales volume
            }
            @Override
            public int getBatchSize() { return count; }
        });
    }
}
