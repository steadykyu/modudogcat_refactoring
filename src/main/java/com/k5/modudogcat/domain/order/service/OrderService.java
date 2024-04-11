package com.k5.modudogcat.domain.order.service;

import com.k5.modudogcat.domain.cart.entity.Cart;
import com.k5.modudogcat.domain.cart.service.CartService;
import com.k5.modudogcat.domain.order.dto.OrderDto;
import com.k5.modudogcat.domain.order.entity.Order;
import com.k5.modudogcat.domain.order.entity.OrderProduct;
import com.k5.modudogcat.domain.order.mapper.OrderMapper;
import com.k5.modudogcat.domain.order.repository.OrderRepository;
import com.k5.modudogcat.domain.product.entity.Product;
import com.k5.modudogcat.domain.product.service.ProductService;
import com.k5.modudogcat.domain.user.entity.User;
import com.k5.modudogcat.domain.user.service.UserService;
import com.k5.modudogcat.exception.BusinessLogicException;
import com.k5.modudogcat.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;
    private final OrderMapper mapper;
    @Value("${config.domain}")
    private String domain;
    //==============================
    //            화면 로직
    //==============================
    @Transactional
    public Long postOrder(OrderDto.Post postDto){
        Order order = mapper.orderPostToOrder(postDto);
        Order findOrder = createOrder(order);
        return findOrder.getOrderId();
    }
    @Transactional
    public OrderDto.Response patchOrder(OrderDto.Patch patchDto,
                                        Long userId){
        Order order = mapper.orderPatchToOrder(patchDto);
        Order updateOrder = updateOrder(order, userId);
        OrderDto.Response response = mapper.orderToOrderResponse(updateOrder, domain);
        return response;
    }
    @Transactional
    public OrderDto.Response getOrder(Long orderId, Long userId){
        Order findOrder = findOrder(orderId, userId);
        OrderDto.Response response = mapper.orderToOrderResponse(findOrder, domain);
        return response;
    }

    public OrderDto.PagingResponse findBuyerOrders(Pageable pageable, Long userId){
        Page<Order> pageOrders = findAllBuyerOrders(pageable, userId);
        OrderDto.PagingResponse pagingResponse = mapper.pageToPagingResponse(pageOrders, domain);
        return pagingResponse;
    }

    //==============================
    //            핵심 비즈니스 로직
    //==============================
    // fixme : (회원이 계속 뒤로가기를 눌러서) 주문이 무한적으로 생성되는것을 막을 방법을 고민해보자. -> redirect
    public Order createOrder(Order order){
        // 영속성 회원 엔티티 넣어주기
        User findUser = userService.findVerifiedUserIncludeCart(order.getUser().getUserId());
        order.setUserAddOrder(findUser);
        // 장바구니들 속 세부상품 비워주기
        cartService.removeCartProductsByCarts(findUser.getCart());

        // 주문 상품 생성을 위한 상품 조회
        List<Long> productIds = order.getOrderProductList().stream()
                .map(orderProduct -> orderProduct.getProduct().getProductId())
                .collect(Collectors.toList());
        List<Product> products = productService.findProductsIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId
                        , Function.identity()));
        // OrderProduct에 조회된 상품 정보 매핑
        for (OrderProduct orderProduct : order.getOrderProductList()) {
            Product product = productMap.get(orderProduct.getProduct().getProductId());
            if (product != null) {
                orderProduct.setProduct(product);
            }
        }

        stockMinusCount(order);
        Order savedOrder = orderRepository.save(order);
        return savedOrder;
    }
    private void stockMinusCount(Order order) {
        // 주문속 상품의 주문 개수 만큼, 상품의 재고를 빼준다.
        for (OrderProduct orderProduct : order.getOrderProductList()) {
            Product findProduct = orderProduct.getProduct();
            long updatedStock = findProduct.getStock() - orderProduct.getProductCount();
            if (updatedStock < 0) {
                throw new BusinessLogicException(ExceptionCode.NO_STOCK);
            }
            findProduct.setStock(updatedStock);
            orderProduct.setProduct(findProduct);
        }
    }

    public Order updateOrder(Order order, Long userId){
        Long orderId = order.getOrderId();
        Order findOrder = findVerifiedOrderById(orderId);
        verifyCorrectUser(orderId, userId);

        Optional.ofNullable(order.getReceiver())
                .ifPresent(newReceiver -> findOrder.setReceiver(newReceiver));
        Optional.ofNullable(order.getPhone())
                .ifPresent(newPhone -> findOrder.setPhone(newPhone));
        Optional.ofNullable(order.getReceivingAddress())
                .ifPresent(newAddress -> findOrder.setReceivingAddress(newAddress));
        // todo : 상품의 개수 수정시, 수정 되도록 기능 개발 필요

        return orderRepository.save(findOrder);
    }
    public Order findOrder(Long orderId, Long userId){
        Order findOrder = findVerifiedOrderById(orderId);
        verifyCorrectUser(orderId, userId);

        return findOrder;
    }

    public Order findVerifiedOrderById(Long orderId){
        Order findOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND);
                });
        verifiedActiveOrder(findOrder);
        return findOrder;
    }

    public Page<Order> findAllBuyerOrders(Pageable pageable, Long userId){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("createdAt").descending());
        Page<Order> findOrders = orderRepository.findAllByOrderStatusNotLikeAndUserUserId(Order.OrderStatus.ORDER_DELETE, userId, of);

        return findOrders;
    }

    @Transactional
    public void removeOrder(Long orderId, Long userId){
        Order findOrder = findVerifiedOrderById(orderId);
        verifyCorrectUser(orderId, userId);

        findOrder.setOrderStatus(Order.OrderStatus.ORDER_DELETE);
    }
    
    public void verifiedActiveOrder(Order findOrder){
        if(findOrder.getOrderStatus().getStatus().equals("삭제된주문")) {
            throw new BusinessLogicException(ExceptionCode.REMOVED_ORDER);
        }
    }

    public void verifyCorrectUser(Long orderId, Long LoginUserId){
        Order findOrder = findVerifiedOrderById(orderId);
        Long dbUserId = findOrder.getUser().getUserId();
        if(LoginUserId != dbUserId){
            throw new BusinessLogicException(ExceptionCode.NOT_ALLOWED_USER);
        }
    }
}
