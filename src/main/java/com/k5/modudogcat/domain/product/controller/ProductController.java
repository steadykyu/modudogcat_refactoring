package com.k5.modudogcat.domain.product.controller;

import com.k5.modudogcat.domain.product.dto.ProductDto;
import com.k5.modudogcat.domain.product.service.ProductService;
import com.k5.modudogcat.dto.MultiResponseDto;
import com.k5.modudogcat.dto.SingleResponseDto;
import com.k5.modudogcat.util.UriCreator;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 임시 상품 등록
    @PostMapping
    public ResponseEntity postProduct(@RequestPart(name = "post") ProductDto.Post postDto,
                                      @RequestPart(required = false, name = "thumbnailImage") MultipartFile thumbnailImage,
                                      @RequestPart(required = false, name = "productDetailImages") List<MultipartFile> productDetailImages){

        Long sellerId = productService.findSellerIdByToken();
        postDto.setSellerId(sellerId);

        System.out.println(productDetailImages.get(0).getSize());
        Long findProductId = productService.postProduct(postDto, thumbnailImage, productDetailImages);

        URI location = UriCreator.createUri("/products", findProductId);

        return ResponseEntity.created(location)
                .body("ProductImage uploaded successfully");
    }
    // todo: 상품 수정 핸들러 메서드 -> 프론트쪽 구현이 안돼있음.

    // todo: 판매자
    @GetMapping("/{product-id}")
    public ResponseEntity getProduct(@PathVariable("product-id") Long productId){
        ProductDto.Response response = productService.getProduct(productId);
        return new ResponseEntity<>(
                new SingleResponseDto(response), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getProducts(Pageable pageable){
        ProductDto.PagingResponse pagingResponse = productService.getProducts(pageable);
        List<ProductDto.Response> responses = pagingResponse.getResponses();
        Page productPages = pagingResponse.getPagingProducts();

        return new ResponseEntity<>(
                new MultiResponseDto<>(responses, productPages), HttpStatus.OK);
    }

    @DeleteMapping("/{product-id}")
    public ResponseEntity deleteProduct(@PathVariable("product-id") Long productId){
        productService.removeProduct(productId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
