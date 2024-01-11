package com.k5.modudogcat.domain.review.service;

import com.k5.modudogcat.domain.review.dto.ReviewDto;
import com.k5.modudogcat.domain.review.entity.reviewImage.Image;
import com.k5.modudogcat.domain.review.entity.Review;
import com.k5.modudogcat.domain.review.mapper.ReviewMapper;
import com.k5.modudogcat.domain.review.repository.ReviewRepository;
import com.k5.modudogcat.domain.user.entity.User;
import com.k5.modudogcat.domain.user.service.UserService;
import com.k5.modudogcat.exception.BusinessLogicException;
import com.k5.modudogcat.exception.ExceptionCode;
import com.k5.modudogcat.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ReviewMapper reviewMapper;

    @Value("${config.domain}")
    private String domain;
    //===================================
    // 화면 비즈니스 로직
    //===================================
    @Transactional
    public Long postReview(ReviewDto.Post postDto, List<MultipartFile> images) throws IOException {

        Review review = reviewMapper.reviewPostToReview(postDto);
        // todo: imageList에 null이 들어가야하는데, proxy 값이 들어가있음 이거 수정필요
        List<Image> imageList = reviewMapper.multipartFilesToImages(images);
        Review findReview = createReview(review, imageList);
        return findReview.getReviewId();
    }
    @Transactional
    public ReviewDto.Response getReview(Long reviewId){
        Review findReview = findReview(reviewId);
        ReviewDto.Response response = reviewMapper.reviewToResponse(findReview, domain);
        return response;
    }
    @Transactional
    public List<ReviewDto.Response> getUserReviews(Pageable pageable, Long userId){
        Page<Review> reviewPages = findUserReviews(pageable, userId);
        List<Review> reviews = reviewPages.getContent();
        List<ReviewDto.Response> responses = reviewMapper.reviewsToResponses(reviews, domain);
        return responses;
    }
    @Transactional
    public List<ReviewDto.Response> getProductReviews(Pageable pageable, Long productId){
        Page<Review> reviewPages = findProductReviews(pageable, productId);
        List<Review> reviews = reviewPages.getContent();
        List<ReviewDto.Response> responses = reviewMapper.reviewsToResponses(reviews, domain);
        return responses;
    }
    //===================================
    // 핵심 비즈니스 로직
    //===================================
    public Review createReview(Review review, List<Image> images){
        // todo: 해당 유저의 상품, 리뷰가 존재할 시, 리뷰가 이미 존재하고 있음을 알리자.
        // todo : 리뷰가 생성되면, 상품에도 리뷰가 추가되어야한다.
        // refact: 이거 Image 빌때 문제가 생기는듯 연관관계로 인해
        Review saveReviewed = reviewRepository.save(review);
        if(images != null){
            List<Image> collect = images.stream()
                    .map(image -> {
                        saveReviewed.addImage(image);
                        return image;
                    }).collect(Collectors.toList());
        }
        return saveReviewed;
    }

    public Review findReview(Long reviewId){
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        Review verifiedReview = optionalReview.orElseThrow(() -> {
            throw new BusinessLogicException(ExceptionCode.REVIEW_NOT_FOUND);
        });

        verifiedActiveReview(verifiedReview);
        return verifiedReview;
    }

    public Page<Review> findUserReviews(Pageable pageable, Long userId){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());
        Page<Review> findReviews = reviewRepository.findAllByReviewStatusAndUserUserId(Review.ReviewStatus.REVIEW_ACTIVE, userId, of);

        return findReviews;
    }

    public Page<Review> findProductReviews(Pageable pageable, Long productId){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());
        Page<Review> findReviews = reviewRepository.findAllByReviewStatusAndProductProductId(Review.ReviewStatus.REVIEW_ACTIVE, productId, of);

        return findReviews;
    }
    @Transactional
    public void removeReview(Long reviewId){
        Review findReview = findReview(reviewId);
        verifiedActiveReview(findReview);
        findReview.setReviewStatus(Review.ReviewStatus.REVIEW_DELETE);
//        reviewRepository.save(findReview);
    }
    @Transactional
    public void removeReviewByAdmin(Long reviewId, Long userId){
        User findUser = userService.findVerifiedUserById(userId);
        // 관리자 검증
        userService.verifiedAdminRole(findUser);
        removeReview(reviewId);
    }

    private void verifiedActiveReview(Review verifiedReview){
        if(verifiedReview.getReviewStatus().getStatus().equals("삭제된리뷰")) {
            throw new BusinessLogicException(ExceptionCode.REMOVED_REVIEW);
        }
    }
}
