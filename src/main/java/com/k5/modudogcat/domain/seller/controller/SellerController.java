package com.k5.modudogcat.domain.seller.controller;

import com.k5.modudogcat.domain.product.dto.ProductDto;
import com.k5.modudogcat.domain.seller.dto.SellerDto;
import com.k5.modudogcat.domain.seller.service.SellerService;
import com.k5.modudogcat.dto.MultiResponseDto;
import com.k5.modudogcat.dto.SingleResponseDto;
import com.k5.modudogcat.security.service.AuthenticationService;
import com.k5.modudogcat.util.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final AuthenticationService authenticationService;

    //판매자의 판매자 회원가입 신청
    @PostMapping
    public ResponseEntity postSeller(@Valid @RequestBody SellerDto.Post postDto) {
        Long sellerId = sellerService.postSeller(postDto);
        URI location = UriCreator.createUri("/sellers/", sellerId);
        return ResponseEntity.created(location).build();
    }

    //판매자의 판매자 페이지 정보 변경 (주소, 전화번호, 이메일)
    @PatchMapping("/{seller-id}")
    public ResponseEntity patchSeller(@PathVariable("seller-id") @Positive Long sellerId,
                                      @RequestBody SellerDto.Patch patch) {

        patch.setSellerId(sellerId);
        SellerDto.Response response = sellerService.patchSeller(patch);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    //판매자의 판매자 페이지 조회
    @GetMapping("/my-page")
    public ResponseEntity getSeller(){

        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(principal);
        SellerDto.Response response = sellerService.getSeller(userId);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    //판매자의 판매 중인 상품 목록 조회
    @GetMapping("/selling")
    public ResponseEntity getSellingProducts(Pageable pageable) {
        ProductDto.PagingResponse pagingResponse = sellerService.getSellingProducts(pageable, authenticationService.findSellerIdByTokenUserId());

        List<ProductDto.Response> responseList = pagingResponse.getResponses();
        Page pageProducts = pagingResponse.getPagingProducts();

        return new ResponseEntity<>(new MultiResponseDto<>(responseList, pageProducts), HttpStatus.OK);
    }


    //판매자의 판매 중인 상품 삭제
    @DeleteMapping("/selling/{product-id}")
    public ResponseEntity deleteSellingProduct(@PathVariable("product-id") @Positive Long productId){
    sellerService.removeProduct(productId,authenticationService.findSellerIdByTokenUserId());
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


}
