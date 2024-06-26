package com.k5.modudogcat.domain.order.entity;

import com.k5.modudogcat.audit.Auditable;
import com.k5.modudogcat.domain.seller.entity.Seller;
import com.k5.modudogcat.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_table")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Order extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(length = 20, nullable = false)
    private String receiver;
    @Column(length = 20, nullable = false)
    private String phone;
    @Column(nullable = false)
    private String receivingAddress;
    private Long totalPrice;
    @Enumerated(value = EnumType.STRING)
    private PayMethod payMethod = PayMethod.NO_BANK_BOOK;
    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.ORDER_ACTIVE;
    @OneToMany(mappedBy = "order", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    @BatchSize(size = 12)
    private List<OrderProduct> orderProductList = new ArrayList<>();

    public enum PayMethod{
        NO_BANK_BOOK("무통장");

        @Getter
        private final String status;
        PayMethod(String status){
            this.status = status;
        }
    }
    public enum OrderStatus{
        ORDER_ACTIVE("활성화주문"),
        ORDER_DELETE("삭제된주문");
        @Getter
        private final String status;
        OrderStatus(String status){
            this.status = status;
        }
    }

    /**
     * 연관관계 편의 메서드
     */
    public void setUserAddOrder(User user){
        this.user = user;
        user.getOrderList().add(this);
    }
}
