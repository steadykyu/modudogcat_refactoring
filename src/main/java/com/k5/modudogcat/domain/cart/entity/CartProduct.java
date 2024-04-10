package com.k5.modudogcat.domain.cart.entity;

import com.k5.modudogcat.domain.product.entity.Product;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

@Entity
@BatchSize(size = 100)
@Getter
@Setter
public class CartProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartProductId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer productCount = 1;

    public void addCart(Cart cart){
        this.cart = cart;
        if(!cart.getCartProductList().contains(this)){
            cart.getCartProductList().add(this);
        }
    }
}
