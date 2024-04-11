package com.k5.modudogcat.domain.product.service;

import com.k5.modudogcat.domain.admin.service.AdminService;
import com.k5.modudogcat.domain.product.dto.ProductDto;
import com.k5.modudogcat.domain.product.entity.Product;
import com.k5.modudogcat.domain.product.entity.productImage.ProductDetailImage;
import com.k5.modudogcat.domain.product.mapper.ProductMapper;
import com.k5.modudogcat.domain.product.repository.ProductRepository;
import com.k5.modudogcat.domain.seller.service.SellerService;
import com.k5.modudogcat.exception.BusinessLogicException;
import com.k5.modudogcat.exception.ExceptionCode;
import com.k5.modudogcat.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper mapper;
    @Value("${config.domain}")
    private String domain;
    private final ProductRepository productRepository;
    private final AdminService adminService;
    private final SellerService sellerService;
    private final AuthenticationService authenticationService;
    //===================================
    // 화면 비즈니스 로직
    //===================================
    @Transactional
    public Long postProduct(ProductDto.Post postDto, MultipartFile thumbnailImage,
                            List<MultipartFile> productDetailImages){
        Product product = mapper.productPostToProduct(postDto);
        Map<String, Object> thumbnailMap = mapper.multipartFileToThumbnailImage(thumbnailImage);
        List<ProductDetailImage> productDetailImageList = mapper.multipartFilesToDetailsImages(productDetailImages);
        Product findProduct = createProduct(product, thumbnailMap, productDetailImageList);
        return findProduct.getProductId();
    }

    @Transactional
    public ProductDto.Response getProduct(Long productId){
        Product findProduct = findProduct(productId);
        ProductDto.Response response = ProductMapper.productToResponse(findProduct, domain);
        return response;
    }

    @Transactional
    public ProductDto.PagingResponse getProducts(Pageable pageable){
        Page<Product> pageProduct = findProducts(pageable);
        ProductDto.PagingResponse pagingResponses = mapper.pageToPagingResponse(pageProduct, domain);
        return pagingResponses;
    }
    //===================================
    // 핵심 비즈니스 로직
    //===================================

        // 썸네일만 넣을수도, 본문을 넣을수도, 둘다 안넣을 수도 있음
    public Product createProduct(Product product, Map<String, Object> thumbnailMap, List<ProductDetailImage> productDetailImageList){
        if(thumbnailMap != null){
            product.setThumbnailImage((byte[]) thumbnailMap.get("파일"));
            product.setThumbnailImageType((String) thumbnailMap.get("타입"));
        }


        if(productDetailImageList != null){
            productDetailImageList.stream()
                    .map(image -> {
                        product.addProductDetailImages(image);
                        return image;
                    })
                    .collect(Collectors.toList());
        }


        Product savedProduct = productRepository.save(product);

        return savedProduct;
    }

    public Product findProduct(Long productId){
        Optional<Product> optionalProduct = productRepository.findById(productId);
        Product verifiedProduct = optionalProduct.orElseThrow(() -> {
            throw new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND);
        });

        verifiedActiveProduct(verifiedProduct);
        return verifiedProduct;
    }
    public Page<Product> findProducts(Pageable pageable){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());
        Page<Product> findProducts = productRepository.findAllByProductStatusNotLike(Product.ProductStatus.PRODUCT_DELETE, of);

        return findProducts;
    }
    // todo: 판매자Id를 통해 해당 판매자가 가진 Product만 가져오도록 하는 로직
    // + 삭제된 상품 및 활성화된 상품등 동적 쿼리를 해볼 수 있을듯

    public List<Product> findProductsIds(List<Long> productIds){
        List<Product> productsByIdIn = productRepository.findProductsByProductIdIn(productIds);
        return productsByIdIn;
    }

    @Transactional
    // 상품은 상품을 올린 판매자 이거나, 관리자 일 경우만 삭제가 가능하다.
    public void removeProduct(Long productId){
        Product findProduct = findProduct(productId);
        if(!(isAdmin() || isCorrectSeller(findProduct))){
            throw new BusinessLogicException(ExceptionCode.NOT_ADMIN_OR_SELLER_ALLOWED);
        }
        findProduct.setProductStatus(Product.ProductStatus.PRODUCT_DELETE);
    }

    // fixme: 로그인 정보들 중복코드발생하는데 수정 못하나?
    public Long findSellerIdByToken(){
        Long sellerId = authenticationService.findSellerIdByTokenUserId();
        return sellerId;
    }
    // 로그인한 판매자의 Id가 옳은지 확인
    public boolean isCorrectSeller(Product product){
        Long sellerId = findSellerIdByToken();

        if(product.getSeller().getSellerId() == sellerId){
            return true;
        }
        return false;
    }

    public boolean isAdmin() {
        Long userId = authenticationService.getUserIdByToken();

        return adminService.verifiedHasAdminRole(userId);
    }

    public void verifiedActiveProduct(Product verifiedProduct){
        if(verifiedProduct.getProductStatus().getStatus().equals("삭제된상품")){
            throw new BusinessLogicException(ExceptionCode.REMOVED_PRODUCT);
        }
    }

    public Map<String, Object> findThumbnail(Long productId){
        Map<String, Object> thumbnailMap = new HashMap<>();
        Product findProduct = findProduct(productId);
        thumbnailMap.put("파일",findProduct.getThumbnailImage());
        thumbnailMap.put("타입",findProduct.getThumbnailImageType());
        return thumbnailMap;
    }
}
