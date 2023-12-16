package com.k5.modudogcat.domain.admin.mapper;

import com.k5.modudogcat.domain.admin.dto.AdminDto;
import com.k5.modudogcat.domain.seller.entity.Seller;
import com.k5.modudogcat.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    List<AdminDto.Response> sellersToAdminResponseDto(List<Seller> sellers);

    AdminDto.Response sellerToAdminResponseDto(Seller updateApproval);

    default AdminDto.PagingDto pageToPageResponse(Page<Seller> pageSellers){
        List<Seller> sellers = pageSellers.getContent();
        List<AdminDto.Response> responseList = sellersToAdminResponseDto(sellers);
        return new AdminDto.PagingDto(responseList,pageSellers);
    }
//    @Mapping(source = "sellerId", target = "seller.sellerId")
//    User sellerDtoToUser(AdminDto.SellerDto sellerDtoToUser);
//
//    AdminDto.SellerDto sellerToSellerDto(Seller updateApproval);
}
