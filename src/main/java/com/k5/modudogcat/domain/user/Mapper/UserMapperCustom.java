package com.k5.modudogcat.domain.user.Mapper;

import com.k5.modudogcat.domain.cart.entity.Cart;
import com.k5.modudogcat.domain.user.dto.UserDto;
import com.k5.modudogcat.domain.user.entity.User;

public class UserMapperCustom {

    public static User userPostToUser(UserDto.Post postDto){
        if ( postDto == null ) {
            return null;
        }
        // (1) 생성자 방식으로 교환
        User user = new User(postDto.getLoginId()
                ,postDto.getName()
                ,postDto.getPassword()
                ,postDto.getEmail()
                ,postDto.getAddress()
                );
        user.addCart(new Cart()); // (2) 편의 메서드 추가

        return user;
    }
}
