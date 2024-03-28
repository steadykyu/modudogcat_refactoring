package com.k5.modudogcat.domain.cart.controller;

import com.k5.modudogcat.domain.cart.dto.CartDto;
import com.k5.modudogcat.domain.cart.entity.CartProduct;
import com.k5.modudogcat.domain.cart.mapper.CartMapper;
import com.k5.modudogcat.domain.cart.service.CartService;
import com.k5.modudogcat.security.service.AuthenticationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
@Setter
@Getter
public class CartController {
    private final CartService cartService;
    private final AuthenticationService authenticationService;
    // 장바구니에 상품 등록요청
    @PostMapping("/products/{product-id}")
    public ResponseEntity postProducts(@PathVariable("product-id") Long productId){

        Long userId = authenticationService.getUserIdByToken();
        Long cartId = cartService.findVerifiedCartByUserId(userId).getCartId();
        cartService.addToCart(cartId, productId);

        return new ResponseEntity(HttpStatus.CREATED);
    }
    // 장바구니 조회
    @GetMapping
    public ResponseEntity getCartProducts(Pageable pageable){
        Long userId = authenticationService.getUserIdByToken();
        CartDto.Response response = cartService.getCartProducts(pageable, userId);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    // 장바구니의 상품 개수 추가 v1
    @PatchMapping("/products/{product-id}/plus")
    public ResponseEntity plusProductCount(@PathVariable("product-id") Long productId) {
        Long userId = authenticationService.getUserIdByToken();
        cartService.plusCount(productId, userId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/products/{product-id}/minus")
    public ResponseEntity minusProductCount(@PathVariable("product-id") Long productId) {
        Long userId = authenticationService.getUserIdByToken();
        cartService.minusCount(productId, userId);

        return new ResponseEntity(HttpStatus.OK);
    }
    // 장바구니 속 상품 삭제
    @DeleteMapping("/products/{product-id}")
    public ResponseEntity deleteProducts(@PathVariable("product-id") Long productId){

        Long userId = authenticationService.getUserIdByToken();
        cartService.removeCartProduct(productId, userId);

        return new ResponseEntity(HttpStatus.CREATED);
    }

}
