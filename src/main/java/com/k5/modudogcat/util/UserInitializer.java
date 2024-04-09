package com.k5.modudogcat.util;

import com.k5.modudogcat.domain.admin.service.AdminService;
import com.k5.modudogcat.domain.seller.dto.SellerDto;
import com.k5.modudogcat.domain.seller.entity.Seller;
import com.k5.modudogcat.domain.seller.service.SellerService;
import com.k5.modudogcat.domain.user.dto.UserDto;
import com.k5.modudogcat.domain.user.entity.User;
import com.k5.modudogcat.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {
    private final UserService userService;
    private final SellerService sellerService;
    private final AdminService adminService;
    private final Environment env;
    private final EntityManager em;

    @Override
    public void run(String... args) throws Exception {

        String ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto");
        if ("create".equals(ddlAuto)) {
            // ddl-auto가 create로 설정된 경우에만 실행할 로직
            InitializeDatabase();
        }

    }

    public void InitializeDatabase() {
        UserDto.Post buyer = new UserDto.Post("buyer", "구매자", "buyer1234!","buyer@google.com","서울특별시 구로구 구일로4길 57");
        SellerDto.Post seller = new SellerDto.Post("seller", "seller1234!", "판매자", "seller@google.com", "1244567894"
                ,"부산특별시 서면 구일로4길 57", "01012345678","신한은행","012345678901");
        UserDto.Post admin = new UserDto.Post("admin", "관리자", "admin1234!","admin@google.com","서울특별시 중구 청계천로4길 57");

        Long buyerId = userService.postUser(buyer);
        Long sellerId = sellerService.postSeller(seller);
        Long adminId = userService.postUser(admin);


        // 판매자를 회원으로 인정
        adminService.giveApproveToSeller(sellerId);

        System.out.println("------------------------------------------------------------------------------");
        System.out.println(" 초기 회원 생성 완료");
        System.out.println("------------------------------------------------------------------------------");
    }
}
