package com.k5.modudogcat.domain.order.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.k5.modudogcat.domain.order.entity.OrderProduct;
import com.k5.modudogcat.domain.product.dto.ProductDto;
import com.k5.modudogcat.domain.product.entity.Product;
import com.k5.modudogcat.util.OrderProductStatusDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public class OrderProductDto {
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        private Long productId;
        private Long productCount;
    }

    @Getter
    @Setter
    public static class Response {

        Product product = new Product();
        private Long productCount;
        private String productName = product.getName();
        private Long productPrice = product.getPrice();

        public Response(Long productCount, String productName, Long productPrice) {
            this.productCount = productCount;
            this.productName = productName;
            this.productPrice = productPrice;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseIncludeProduct {
        private Long productCount;
        private String parcelNumber;
        private OrderProduct.OrderProductStatus orderProductStatus;
        private ProductDto.Response productResponse;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class sellerResponse {
        private Long orderProductId;
        private String productName;
        private Long productPrice;
        private Long productCount;
        private LocalDateTime createdAt;
        private String receiver;
        private String phone;
        private String receivingAddress;
        @JsonDeserialize(using = OrderProductStatusDeserializer.class)
        private OrderProduct.OrderProductStatus orderProductStatus;
        private String parcelNumber;
    }
    //@Getter 붙이고 내부 생성자 만들고 @Builder 붙여서 (순서 상관없이, 넣고 싶은 인자만 넣을 수 있게) 만들기

    @Getter
    @Setter
    @AllArgsConstructor
    public static class patch {
        @Nullable
        //@Pattern(regexp = "^[0-9]+$")
        private String parcelNumber;
        @JsonDeserialize(using = OrderProductStatusDeserializer.class)
        private OrderProduct.OrderProductStatus orderProductStatus;
    }
}

