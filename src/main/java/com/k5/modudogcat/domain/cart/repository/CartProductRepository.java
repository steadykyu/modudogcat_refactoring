package com.k5.modudogcat.domain.cart.repository;

import com.k5.modudogcat.domain.cart.entity.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    Optional<CartProduct> findByProductProductIdAndCartCartId(Long productId, Long cartId);
    List<CartProduct> findAllByCartCartId(Long cartId);
    void deleteByProductProductIdAndCartCartId(Long productId, Long cartId);
    void deleteAllByCartCartIdIn(List<Long> cartIds);

    @Modifying(clearAutomatically = true) // em.clear()
    @Query("delete CartProduct cp where cp.cart.cartId in :cartIds")
    void deleteAllByCartIds(@Param("cartIds") List<Long> cartIds);
}