package com.k5.modudogcat.domain.order.mapper;

import com.k5.modudogcat.domain.order.dto.OrderDto;
import com.k5.modudogcat.domain.order.dto.OrderProductDto;
import com.k5.modudogcat.domain.order.entity.Order;
import com.k5.modudogcat.domain.order.entity.OrderProduct;
import com.k5.modudogcat.domain.product.dto.ProductDto;
import com.k5.modudogcat.domain.product.entity.Product;
import com.k5.modudogcat.domain.product.mapper.ProductMapper;
import com.k5.modudogcat.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderMapper {
    Order orderPatchToOrder(OrderDto.Patch patchDto);
    default List<OrderDto.Response> ordersToOrdersResponse(List<Order> orderList, String domain){
        if ( orderList == null ) {
            return null;
        }

        List<OrderDto.Response> list = new ArrayList<OrderDto.Response>( orderList.size() );
        for ( Order order : orderList ) {
            list.add( orderToOrderResponse( order , domain) );
        }

        return list;
    }

    default Order orderPostToOrder(OrderDto.Post postDto){
        if ( postDto == null ) {
            return null;
        }
        User fkUser = new User();
        fkUser.setUserId(postDto.getUserId());

        Order order = new Order();
        order.setReceiver( postDto.getReceiver() );
        order.setPhone( postDto.getPhone() );
        order.setReceivingAddress( postDto.getReceivingAddress() );
        order.setTotalPrice( postDto.getTotalPrice() );
        // DTO에서 OrderProduct 값 넣기
        List<OrderProduct> orderProducts = postDto.getOrderProductDtos().stream()
                .map(orderProductDto -> {
                    OrderProduct orderProduct = new OrderProduct();
                    Product fkProduct = new Product();
                    fkProduct.setProductId(orderProductDto.getProductId());
                    orderProduct.setProduct(fkProduct);
                    orderProduct.setProductCount(orderProductDto.getProductCount());
                    // 편의메서드 - 양방향 연관관계
                    orderProduct.setOrderAddOrderProduct(order);
                    return orderProduct;
                }).collect(Collectors.toList());

        // Order에 주입
        order.setUser(fkUser);
//        order.setOrderProductList(orderProducts);
        return order;
    }

    default OrderDto.Response orderToOrderResponse(Order order, String domain) {
        if ( order == null ) {
            return null;
        }

        OrderDto.Response response = new OrderDto.Response();

        response.setUserId( orderUserUserId( order ) );
        response.setOrderId( order.getOrderId() );
        response.setReceiver( order.getReceiver() );
        response.setPhone( order.getPhone() );
        response.setReceivingAddress( order.getReceivingAddress() );
        response.setTotalPrice( order.getTotalPrice() );
        response.setPayMethod( order.getPayMethod() );
        response.setOrderStatus( order.getOrderStatus() );
        response.setCreatedAt( order.getCreatedAt() );
        response.setModifiedAt( order.getModifiedAt() );

        ProductDto.Response productResponse = new ProductDto.Response();

        List<OrderProductDto.ResponseIncludeProduct> orderProductResponseIncludeProduct = order.getOrderProductList().stream()
                .map(orderProduct -> {
                    OrderProductDto.ResponseIncludeProduct responseIncludeProduct = new OrderProductDto.ResponseIncludeProduct();
                    responseIncludeProduct.setProductCount(orderProduct.getProductCount());
                    responseIncludeProduct.setParcelNumber(orderProduct.getParcelNumber());
                    responseIncludeProduct.setOrderProductStatus(orderProduct.getOrderProductStatus());
                    responseIncludeProduct.setProductResponse(ProductMapper.productToResponse(orderProduct.getProduct(), domain,true));
                    return responseIncludeProduct;
                }).collect(Collectors.toList());
        response.setResponseIncludeProducts(orderProductResponseIncludeProduct);

        return response;
    }

    default Long orderUserUserId(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        Long userId = user.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    List<OrderDto.Response> orderListToResponseDtoList(List<Order> orders);

    OrderDto.Response orderToOrderResponseDto(Order patchOrderStatus);

    default OrderDto.PagingResponse pageToPagingResponse(Page<Order> pageOrders, String domain){
        List<Order> orders = pageOrders.getContent();
        List<OrderDto.Response> responses = ordersToOrdersResponse(orders, domain);
        return new OrderDto.PagingResponse(responses, pageOrders);
    }
}
