package com.k5.modudogcat.domain.cart.service;

import com.k5.modudogcat.domain.cart.dto.CartDto;
import com.k5.modudogcat.domain.cart.entity.Cart;
import com.k5.modudogcat.domain.cart.entity.CartProduct;
import com.k5.modudogcat.domain.cart.mapper.CartMapper;
import com.k5.modudogcat.domain.cart.repository.CartProductRepository;
import com.k5.modudogcat.domain.cart.repository.CartRepository;
import com.k5.modudogcat.domain.product.entity.Product;
import com.k5.modudogcat.domain.product.service.ProductService;
import com.k5.modudogcat.exception.BusinessLogicException;
import com.k5.modudogcat.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;
    @Value("${config.domain}")
    private String domain;

    //===================================
    // 화면 비즈니스 로직
    //===================================
    @Transactional
    public CartDto.Response getCartProducts(Pageable pageable, Long userId){
        Long cartId = findVerifiedCartByUserId(userId).getCartId();
        Page<CartProduct> cartProductPages = findCartProducts(pageable, cartId);
        List<CartProduct> findCartProducts = cartProductPages.getContent();
        CartDto.Response response = cartMapper.cartProductsToResponse(findCartProducts, cartId, domain);
        return response;
    }

    //===================================
    // 핵심 비즈니스 로직
    //===================================
    @Transactional
    public CartProduct addToCart(Long cartId, Long productId){
        // 이미 장바구니에 담겼는지 확인
        verifiedCartProduct(cartId, productId);
        
        CartProduct cartProduct = new CartProduct();
        Cart findCart = findVerifiedCartByCartId(cartId);
        Product findProduct = productService.findProduct(productId);
        cartProduct.setProduct(findProduct);
        cartProduct.addCart(findCart);

        return cartProductRepository.save(cartProduct);
    }

    @Transactional
    public void plusCount(Long productId, Long userId){
        Long cartId = findVerifiedCartByUserId(userId).getCartId();
        CartProduct findCartProduct = findVerfiedCartProduct(productId, cartId);
        if(findCartProduct.getProductCount().equals(findCartProduct.getProduct().getStock())){
            throw new RuntimeException("더 이상 증가시킬 수 없습니다.");
        }else{
            findCartProduct.setProductCount(findCartProduct.getProductCount() + 1);
        }
        cartProductRepository.save(findCartProduct);
    }

    public Cart findVerifiedCartByUserId(Long userId){
        Cart findCart = cartRepository.findByUserUserId(userId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.USER_NO_CART);
                });
        return findCart;
    }

    public Cart findVerifiedCartByCartId(Long cartId){
        Cart findCart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.WRONG_PRODUCT_OR_CART);
                });
        return findCart;
    }
    public Page<CartProduct> findCartProducts(Pageable pageable, Long cartId){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());
        List<CartProduct> findCartProducts = cartProductRepository.findAllByCartCartId(cartId);

        return new PageImpl<>(findCartProducts, of, findCartProducts.size());
    }
    @Transactional
    public void minusCount(Long productId, Long userId){
        Long cartId = findVerifiedCartByUserId(userId).getCartId();
        CartProduct findCartProduct = findVerfiedCartProduct(productId, cartId);
        if(findCartProduct.getProductCount() == 0){
            throw new RuntimeException("더 이상 감소시킬 수 없습니다.");
        }else{
            findCartProduct.setProductCount(findCartProduct.getProductCount() - 1);
        }
        cartProductRepository.save(findCartProduct);
    }
    @Transactional
    public void removeCartProduct(Long productId, Long userId){
        Long cartId = findVerifiedCartByUserId(userId).getCartId();
        cartProductRepository.deleteByProductProductIdAndCartCartId(productId, cartId);
    }

    public CartProduct findVerfiedCartProduct(Long productId, Long cartId){
        CartProduct findCartProduct = cartProductRepository.findByProductProductIdAndCartCartId(productId, cartId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.WRONG_PRODUCT_OR_CART);
                });
        return findCartProduct;
    }

    private void verifiedCartProduct(Long cartId, Long productId){
        Optional<CartProduct> optionalCartProduct = cartProductRepository.findByProductProductIdAndCartCartId(productId, cartId);
        optionalCartProduct.ifPresent(cartProduct -> {
                    throw new BusinessLogicException(ExceptionCode.CART_ALREADY_EXISTS);
                });
    }

    public void removeCartProductsByCarts(List<Cart> carts){
        List<Long> cartIds = carts.stream()
                .map(cart -> cart.getCartId())
                .collect(Collectors.toList());
        cartProductRepository.deleteAllByCartCartIdIn(cartIds);
    }
}
