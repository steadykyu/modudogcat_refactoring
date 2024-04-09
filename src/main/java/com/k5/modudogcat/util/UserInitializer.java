package com.k5.modudogcat.util;

import com.k5.modudogcat.domain.user.dto.UserDto;
import com.k5.modudogcat.domain.user.entity.User;
import com.k5.modudogcat.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {
    private final UserService userService;
    private final Environment env;

    @Override
    public void run(String... args) throws Exception {

        String ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto");
        if ("create".equals(ddlAuto)) {
            // ddl-auto가 create로 설정된 경우에만 실행할 로직
            InitializeDatabase();
        }

    }

    private void InitializeDatabase() {
        UserDto.Post buyer = new UserDto.Post("buyer", "구매자", "buyer1234!","buyer@google.com","서울특별시 구로구 구일로4길 57");
        UserDto.Post seller = new UserDto.Post("seller", "판매자", "seller1234!","seller@google.com","부산특별시 서면 구일로4길 57");
        UserDto.Post admin = new UserDto.Post("admin", "관리자", "admin1234!","admin@google.com","서울특별시 중구 청계천로4길 57");

        Long buyerId = userService.postUser(buyer);
        Long sellerId = userService.postUser(seller);
        Long adminId = userService.postUser(admin);
        System.out.println("------------------------------------------------------------------------------");
        System.out.println(" 초기 회원 생성 완료");
        System.out.println("------------------------------------------------------------------------------");
    }
}
