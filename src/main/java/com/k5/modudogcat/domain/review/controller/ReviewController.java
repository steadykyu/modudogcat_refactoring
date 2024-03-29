package com.k5.modudogcat.domain.review.controller;

import com.k5.modudogcat.domain.review.dto.ReviewDto;
import com.k5.modudogcat.domain.review.service.ReviewService;
import com.k5.modudogcat.security.service.AuthenticationService;
import com.k5.modudogcat.util.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final AuthenticationService authenticationService;
    // 구매자는 주문을 통해 주문한 상품의 리뷰를 작성할 수 있다.
    // todo: 구매자가 해당 상품을 구매한 적이 있는지 확인하는 로직이 필요한가?
    //  -> 어차피 마이페이지에 등록한 주문에서 리뷰를 등록하므로 구매해서 필요없을듯?
    @PostMapping
    public ResponseEntity postReview(@RequestPart(name = "post") ReviewDto.Post postDto,
                                     @RequestPart(required = false) List<MultipartFile> images
    ) throws IOException {
        Long userId = authenticationService.getUserIdByToken();
        postDto.setUserId(userId);

        Long findReviewId = reviewService.postReview(postDto, images);
        URI location = UriCreator.createUri("/users/" + userId + "/reviews", findReviewId);

        return ResponseEntity.created(location)
                .body("Image uploaded successfully");
    }
// 리뷰 단일조회
    @GetMapping("/{review-id}")
    public ResponseEntity getReview(@PathVariable("review-id") Long reviewId) {
        ReviewDto.Response response = reviewService.getReview(reviewId);

        return new ResponseEntity(response, HttpStatus.OK);
    }
// 구매자 마이페이지 - 리뷰들 조회
    @GetMapping("/userReviews")
    public ResponseEntity getUserReviews(Pageable pageable){
        Long userId = authenticationService.getUserIdByToken();
        List<ReviewDto.Response> responses = reviewService.getUserReviews(pageable, userId);
        return new ResponseEntity(responses, HttpStatus.OK);
    }
// 상품 상세 페이지 - 리뷰들 조회
    @GetMapping("/productReviews/{product-id}")
    public ResponseEntity getProductReviews(@PathVariable("product-id") Long productId,
            Pageable pageable){
        List<ReviewDto.Response> responses = reviewService.getProductReviews(pageable, productId);
        return new ResponseEntity(responses, HttpStatus.OK);
    }
// 구매자가 후기를 삭제하는 메서드
    @DeleteMapping("/userReviews/{review-id}")
    public ResponseEntity deleteReviewByUser(@PathVariable("review-id") Long reviewId){

        reviewService.removeReview(reviewId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
// 관리자가 후기를 삭제하는 메서드
    @DeleteMapping("/userReviews/admin/{review-id}")
    public ResponseEntity deleteReviewByAdmin(@PathVariable("review-id") Long reviewId){
        Long userId = authenticationService.getUserIdByToken();
        reviewService.removeReviewByAdmin(reviewId, userId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
