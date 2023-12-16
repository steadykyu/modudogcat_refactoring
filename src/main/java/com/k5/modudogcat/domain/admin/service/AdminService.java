package com.k5.modudogcat.domain.admin.service;

import com.k5.modudogcat.domain.admin.dto.AdminDto;
import com.k5.modudogcat.domain.admin.mapper.AdminMapper;
import com.k5.modudogcat.domain.seller.entity.Seller;
import com.k5.modudogcat.domain.seller.repository.SellerRepository;
import com.k5.modudogcat.domain.seller.service.SellerService;
import com.k5.modudogcat.domain.user.entity.User;
import com.k5.modudogcat.domain.user.repository.UserRepository;
import com.k5.modudogcat.exception.BusinessLogicException;
import com.k5.modudogcat.exception.ExceptionCode;
import com.k5.modudogcat.security.util.CustomAuthorityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final EntityManager em;
    private final SellerRepository sellerRepository;
    private final SellerService sellerService;
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;
    private final CustomAuthorityUtils customAuthorityUtils;

    //------------------------------------------------------
    //                         화면 로직
    //------------------------------------------------------
    @Transactional
    public AdminDto.Response approvalSeller(Long sellerId){
        Seller verifiedSeller = giveApproveToSeller(sellerId);
        AdminDto.Response response = adminMapper.sellerToAdminResponseDto(verifiedSeller);
        return response;
    }
    @Transactional
    public AdminDto.Response rejectedSeller(Long sellerId) {
        Seller verifiedSeller = giveRejectToSeller(sellerId);
        AdminDto.Response response = adminMapper.sellerToAdminResponseDto(verifiedSeller);
        return response;
    }

    public AdminDto.PagingDto getSellers(Pageable pageable){
        Page<Seller> pageSellers = findSellers(pageable);
        AdminDto.PagingDto pagingDto = adminMapper.pageToPageResponse(pageSellers);
        return pagingDto;
    }

    //------------------------------------------------------
    //                         핵심 비즈니스 로직
    //------------------------------------------------------
    @Transactional
    public Seller giveApproveToSeller(Long sellerId){
        Seller findSeller = findVerifiedSellerById(sellerId);
        findSeller.setSellerStatus(Seller.SellerStatus.SELLER_APPROVE);
        sellerService.verifiedApprovedSeller(findSeller);

        updateSellerToUser(findSeller);
        return findSeller;
    }


    //판매자를 회원으로 인정(판매자 정보를 회원 DB에 저장)
    public void updateSellerToUser(Seller seller) {
        User user = makeUserFromSeller(seller);
        List<String> roles = customAuthorityUtils.createRoles(user);
        user.setRoles(roles);
        userRepository.save(user);
    }

    public User makeUserFromSeller(Seller seller){
        if ( seller == null ) {
            return null;
        }

        User user = new User();

        user.setSeller(seller);
        user.setLoginId( seller.getLoginId() );
        user.setName( seller.getName() );
        user.setPassword( seller.getPassword() );
        user.setEmail( seller.getEmail() );
        user.setAddress( seller.getAddress() );

        return user;
    }
    public Seller giveRejectToSeller(Long sellerId){
        Seller findSeller = findVerifiedSellerById(sellerId);
        findSeller.setSellerStatus(Seller.SellerStatus.SELLER_REJECTED);

        return findSeller;
    }

    //판매자 존재 여부 확인
    public Seller findVerifiedSellerById(Long sellerId) {
        Seller findSeller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.SELLER_NOT_FOUND);
                });

        return findSeller;
    }

    public Page<Seller> findSellers(Pageable pageable) {
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());

        Page<Seller> findSellers = sellerRepository.findAllBySellerStatus(Seller.SellerStatus.SELLER_WAITING, of);
        return findSellers;
    }

    //관리자 권한 확인
    public boolean verifiedHasAdminRole(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.get().getRoles().equals("ADMIN")) {
           return true;
        }
        return false;
    }
}
