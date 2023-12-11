package com.k5.modudogcat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.PathVariable;

@EnableJpaAuditing
@SpringBootApplication(exclude = SecurityAutoConfiguration.class) //Todo: 임시 추가 - 로그인 확인하려면 제거해야 함
//@SpringBootApplication
public class ModudogcatApplication {
	public static void main(String[] args) {

		SpringApplication.run(ModudogcatApplication.class, args);
	// NOTE: 페이지 네이션 점검하기
	}
}
