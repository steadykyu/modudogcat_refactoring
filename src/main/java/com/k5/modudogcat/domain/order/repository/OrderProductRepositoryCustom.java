package com.k5.modudogcat.domain.order.repository;

import com.k5.modudogcat.domain.order.entity.OrderProduct;

import java.util.List;

public interface OrderProductRepositoryCustom {
    public List<OrderProduct> batchSaveOrderProducts(List<OrderProduct> orderProducts, Long orderId);
}
