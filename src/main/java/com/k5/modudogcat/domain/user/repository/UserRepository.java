package com.k5.modudogcat.domain.user.repository;

import com.k5.modudogcat.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByLoginId(String LoginId);
    @Query("select u from User u join fetch u.roles where u.loginId = :loginId")
    Optional<User> findByLoginId(@Param("loginId") String LoginId);
    @Query("select u from User u join fetch u.cart where u.userId = :userId")
    Optional<User> findUserIncludeCartById(@Param("userId") Long userId);
    Optional<User> findByEmail(String email);
    Page<User> findAllByUserStatus(User.UserStatus userStatus, Pageable pageable);
}
