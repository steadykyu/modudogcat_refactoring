package com.k5.modudogcat.domain.user.Mapper;

import com.k5.modudogcat.domain.cart.entity.Cart;
import com.k5.modudogcat.domain.user.dto.UserDto;
import com.k5.modudogcat.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    default User userPostToUser(UserDto.Post postDto){
        if ( postDto == null ) {
            return null;
        }

        User user = new User();
        Cart cart = new Cart();

        user.setLoginId( postDto.getLoginId() );
        user.setName( postDto.getName() );
        user.setPassword( postDto.getPassword() );
        user.setEmail( postDto.getEmail() );
        user.setAddress( postDto.getAddress() );

        user.addCart(cart);

        return user;
    }
    User userPatchToUser(UserDto.Patch patchDto);
    UserDto.Response userToUserResponse(User user);
    List<UserDto.Response> usersToUsersResponse(List<User> userList);

    default UserDto.PagingResponse pageToPagingResponse(Page<User> pageUsers){
        List<User> users = pageUsers.getContent();
        List<UserDto.Response> responses = usersToUsersResponse(users);
        return new UserDto.PagingResponse(responses, pageUsers);
    }
}
