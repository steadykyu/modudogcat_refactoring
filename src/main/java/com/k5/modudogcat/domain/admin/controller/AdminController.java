package com.k5.modudogcat.domain.admin.controller;

import com.k5.modudogcat.domain.admin.dto.AdminDto;
import com.k5.modudogcat.domain.admin.service.AdminService;
import com.k5.modudogcat.domain.seller.entity.Seller;
import com.k5.modudogcat.domain.user.entity.User;
import com.k5.modudogcat.dto.MultiResponseDto;
import com.k5.modudogcat.dto.SingleResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;


    //관리자의 판매자 상태 변경(승인)
    @PatchMapping("/approval/{seller-id}")
    public ResponseEntity approvalSellerStatus(@PathVariable("seller-id") @Positive Long sellerId) {
        AdminDto.Response response = adminService.approvalSeller(sellerId);

        return new ResponseEntity(new SingleResponseDto<>(response), HttpStatus.OK);
    }

//    //관리자의 판매자 상태 변경(거절)
    // Todo: 거절 후, 로그인시 거절된 판매자임을 알리는 예외만들기
    @PatchMapping("/rejected/{seller-id}")
    public ResponseEntity rejectedSeller(@PathVariable("seller-id") @Positive Long sellerId) {

        AdminDto.Response response = adminService.rejectedSeller(sellerId);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }


    //관리자의 회원가입 대기 판매자 리스트 조회
    @GetMapping
    public ResponseEntity getSellers(Pageable pageable) {
        AdminDto.PagingDto pageDtos = adminService.getSellers(pageable);
        List<AdminDto.Response> responseList = pageDtos.getResponseList();
        Page<Seller> pageSellers = pageDtos.getPageSellers();

        return new ResponseEntity(new MultiResponseDto<>(responseList, pageSellers), HttpStatus.OK);
    }

}
