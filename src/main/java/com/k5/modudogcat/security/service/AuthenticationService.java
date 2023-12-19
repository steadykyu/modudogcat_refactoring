package com.k5.modudogcat.security.service;

import com.k5.modudogcat.domain.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationService {
    private final SellerService sellerService;

    public Long getUserIdByToken(){
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(principal);
        return userId;
    }

    public Long findSellerIdByTokenUserId() {
        Long userId = getUserIdByToken();
        return sellerService.findSellerIdById(userId);
    }


}
