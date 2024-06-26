package com.k5.modudogcat.domain.product.entity.productImage;

import com.k5.modudogcat.domain.product.entity.Product;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

@Entity
@BatchSize(size = 100)
@Setter
@Getter
public class ProductDetailImage { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long detailImageId;
    @Lob
    private byte[] image;
    @Column(length = 50)
    private String type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
