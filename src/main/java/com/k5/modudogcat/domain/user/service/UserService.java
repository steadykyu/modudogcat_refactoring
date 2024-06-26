package com.k5.modudogcat.domain.user.service;

import com.k5.modudogcat.domain.admin.entity.Admin;
import com.k5.modudogcat.domain.user.Mapper.UserMapper;
import com.k5.modudogcat.domain.user.dto.UserDto;
import com.k5.modudogcat.domain.user.repository.UserRepository;
import com.k5.modudogcat.exception.BusinessLogicException;
import com.k5.modudogcat.exception.ExceptionCode;
import com.k5.modudogcat.security.util.CustomAuthorityUtils;
import com.k5.modudogcat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 비즈니스 로직 담당 서비스 계층
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils customAuthorityUtils;
    private final UserMapper mapper;

    //--------------------------------------------------------------------------------------------------
    //                                  화면 로직
    //--------------------------------------------------------------------------------------------------
    public Long postUser(UserDto.Post postDto){
        User user = mapper.userPostToUser(postDto);
        User postUser = createUser(user); // 비즈니스 로직 함수
        return postUser.getUserId();
    }
    @Transactional
    public UserDto.Response patchUser(UserDto.Patch patchDto){
        User user = mapper.userPatchToUser(patchDto);
        User updateUser = updateUser(user); // 비즈니스 로직 함수
        UserDto.Response response = mapper.userToUserResponse(updateUser);
        return response;
    }

    public UserDto.Response getUser(Long userId){
        User findUser = findVerifiedUserById(userId); // 비즈니스 로직 함수
        UserDto.Response response = mapper.userToUserResponse(findUser);
        return response;
    }

    public UserDto.PagingResponse getUsers(Pageable pageable){
        Page<User> pageUsers = findUsers(pageable); // 비즈니스 로직

        UserDto.PagingResponse pagingResponse = mapper.pageToPagingResponse(pageUsers);

        return pagingResponse;
    }


    //--------------------------------------------------------------------------------------------------
    //                                  핵심 비즈니스 로직
    //--------------------------------------------------------------------------------------------------
    public User createUser(User user){

        verifiedByLoginId(user);
        setEncodedPassword(user);
        setDefaultRole(user);
        User verifiedUser = verifiedAdmin(user);
        initializeCart(verifiedUser);

        return userRepository.save(verifiedUser);
    }

    private void setDefaultRole(User user) {
        List<String> roles = customAuthorityUtils.createRoles(user);
        user.setRoles(roles);
//        user.getRoles().add("USER");
    }

    public void setEncodedPassword(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Transactional
    public User updateUser(User user){
        Long userId = user.getUserId();
        User findUser = findVerifiedUserById(userId);

        Optional.ofNullable(user.getPassword())
                .ifPresent(newPassword -> findUser.setPassword(passwordEncoder.encode(newPassword)));
        Optional.ofNullable(user.getAddress())
                .ifPresent(newAddress -> findUser.setAddress(newAddress));
        Optional.ofNullable(user.getEmail())
                .ifPresent(newEmail -> findUser.setEmail(newEmail));

//        return userRepository.save(findUser);
        return findUser;
    }

    public User findVerifiedUserById(Long userId){
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
                });

        verifiedActiveUser(findUser);
        return findUser;
    }
    public User findVerifiedUserIncludeCart(Long userId){
        User findUser = userRepository.findUserIncludeCartById(userId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
                });

        verifiedActiveUser(findUser);
        return findUser;
    }
    public User findVerifiedUserByLoginId(String loginId){
        User findUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
                });

        verifiedActiveUser(findUser);
        return findUser;
    }

    private static void verifiedActiveUser(User findUser) {
        if(findUser.getUserStatus().getStatus().equals("삭제된계정")){
            throw new BusinessLogicException(ExceptionCode.REMOVED_USER);
        }else if(findUser.getUserStatus().getStatus().equals("휴면계정")){
            throw new BusinessLogicException(ExceptionCode.SLEEPER_USER);
        }
    }

    public Page<User> findUsers(Pageable pageable){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());
        // Active한 User들만 가져온 후, 페이징 객체로 생성
        Page<User> findUsers = userRepository.findAllByUserStatus(User.UserStatus.USER_ACTIVE, of);

        return findUsers;
    }

    public void removeUser(Long userId){
        User findUser = findVerifiedUserById(userId);
        verifiedActiveUser(findUser);

        findUser.setUserStatus(User.UserStatus.USER_DELETE);
        userRepository.save(findUser);
    }
    private void verifiedByEmail(User user) {
        // 중복 이메일을 검증하는 메서드
        String email = user.getEmail();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        optionalUser.ifPresent(s -> {
            throw new BusinessLogicException(ExceptionCode.USER_EMAIL_EXISTS);
        });
    }
    private void verifiedByLoginId(User user) {
        // 로그인 ID가 존재하는지 검증하는 메서드
        String LoginId = user.getLoginId();
        Optional<User> optionalUser = userRepository.findByLoginId(LoginId);
        optionalUser.ifPresent(s -> {
            throw new BusinessLogicException(ExceptionCode.USER_LOGIN_ID_EXISTS);
        });
    }

    public User verifiedAdmin(User findUser) {
        if (findUser.getRoles().get(0).equals("ADMIN")) {
            Admin admin = new Admin();
            admin.setLoginId(findUser.getLoginId());
            admin.setPassword(findUser.getPassword());
            findUser.setAdmin(admin);
            //findVerifiedAdmin(findUser);
        }
        return findUser;
    }

    private void initializeCart(User user){
        if(user.getRoles().contains("SELLER") || user.getRoles().contains("ADMIN")){
            user.setCart(null);
            return;
        }
    }

    public void verifiedAdminRole(User findUser) {
        if(!findUser.getRoles().get(0).equals("ADMIN")) {
            throw new BusinessLogicException(ExceptionCode.USER_NOT_ADMIN);
        }
    }
}
