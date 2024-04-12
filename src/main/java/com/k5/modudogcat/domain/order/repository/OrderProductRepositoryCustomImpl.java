package com.k5.modudogcat.domain.order.repository;

import com.k5.modudogcat.domain.order.entity.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderProductRepositoryCustomImpl implements OrderProductRepositoryCustom {
    private final JdbcTemplate jdbcTemplate;

    public List<OrderProduct> batchSaveOrderProducts(List<OrderProduct> orderProducts, Long orderId) {
        // Identity 전략 - pk 제외
        // 나머지 전략시 - pk 포함
        String sql = "INSERT INTO order_product (order_product_status, parcel_number, product_count, order_id, product_id, created_at, modified_at)" +
                " VALUES (?, ?, ?, ?, ? ,? ,?)";
        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // i번째 객체를 가져온 후, DB에 삽입한다.
                OrderProduct orderProduct = orderProducts.get(i);
                ps.setString(1, orderProduct.getOrderProductStatus().name());
                ps.setString(2, orderProduct.getParcelNumber());
                ps.setLong(3, orderProduct.getProductCount());
                ps.setLong(4, orderId);
                ps.setLong(5, orderProduct.getProduct().getProductId());
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(6, timestamp);
                ps.setTimestamp(7, timestamp);

                orderProduct.setCreatedAt(LocalDateTime.now());
                orderProduct.setModifiedAt(LocalDateTime.now());
            }

            public int getBatchSize() {
                return orderProducts.size();
            }
        });

        return orderProducts;
    }
}

