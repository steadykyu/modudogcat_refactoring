## ModudogCat - 애완용품 커머스 서비스 프로젝트

### Demo Link
+ https://bit.ly/4cEmNer

### 제작 기간 & 참여 인원
+ 프론트 엔드 : 3명 
+ 백엔드 : 1명

### 사용한 기술(기술 스택)
+ Java, Spring Boot, Spring Data JPA, Spring Security
+ H2, MYSQL
+ AWS Route 64, AWS EC2, AWS RDS, React

### ERD
<blockquote>
<details>
  <summary>ERD 그림</summary>
  <p>
    <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/ERD.png" alt="AWS 아키텍처">
  </p>
</details>
</blockquote>

### 핵심 기능
<blockquote>
<details>
  <summary>1. CSR 아키텍처를 채택하여 RESTful API 호출을 통해 통신하는 쇼핑몰 백엔드 서비스를 설계 및 구현</summary>
  <p> 
    <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/studySample/csr.png" alt="CSR 아키텍처">
  </p>
  <p>
    대부분의 도메인들은 RESTful 설계에 따라 <a href = "https://github.com/steadykyu/modudogcat_refactoring/blob/1ef06b737589db917ec4ff77ddb10bbda566d15d/src/main/java/com/k5/modudogcat/domain/product/controller/ProductController.java#L28-L65">
    Product Controller</a> 의 URL 형식에 맞추어 CRUD의 구현을 나타냅니다.
  </p>
  <p>
    다만 회원 도메인의 경우 로그인시 JWT 토큰을 이용해 개별 회원 리소스를 유추해낼 수 있고 회원의 PK를 보여주지 않는게 보안적으로 좋다고 생각했습니다. 그러므로 <a href = "https://github.com/steadykyu/modudogcat_refactoring/blob/1ef06b737589db917ec4ff77ddb10bbda566d15d/src/main/java/com/k5/modudogcat/domain/user/controller/UserController.java#L32-L75">
    회원 Controller</a> 의 URL로 CRUD를 구현했습니다.
  </p>

 ---
 
</details>

<details> 
  <summary>2. Spring JPA를 이용한 WAS 개발 및 쿼리 성능 최적화</summary>
  <p>
   <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/studySample/JPA_architecture.png" alt="JPA 아키텍처">
  </p>
  <p>
    기본적으로 성능최적화를 위해 모든 Entity는 지연로딩으로 설정하는 방향으로 구현했습니다.        </p>
  <p>
    또한 Hibernate를 기반으로 만들어진 Spring JPA를 통해 ORM 프레임워크를 구현했습니다. 대부분의 기능은 Spring Data JPA의 쿼리메소드를 이용하여 쿼리를 매핑하여 실행시키도록 구현했습니다.
  <p>
    <strong>1) single select Paging query:</strong> 1:N의 Order와 OrderProduct를 사용하는 페이징 기능 과정 N+1 문제가 발생했었습니다. 해당 문제를 해결하고, 대용량 주문 또는 상품을 조회하더라도 시스템 장애가 발생하지 않도록 Batch Processing을 구현했습니다. 
  </p>
  <p>
     <a href = https://github.com/steadykyu/modudogcat_refactoring/blob/cb9746bb7f0c301a038000eb4ce9e100cc6fbbee/src/main/java/com/k5/modudogcat/domain/order/entity/Order.java#L40-L42>Order의 OrderProduct엔티티</a> 는 페이징 크기에 맞게 12개의 배치로 지정해 두었습니다.
  <p>
  <p>
    Order:OrderProduct 그리고 OrderProduct:Product는 1:N, N:1 이므로 <a href = https://github.com/steadykyu/modudogcat_refactoring/blob/cb9746bb7f0c301a038000eb4ce9e100cc6fbbee/src/main/java/com/k5/modudogcat/domain/product/entity/Product.java#L17-L23>Product 엔티티</a> 또한 배치를 지정했습니다. 
  </p>  
    <strong>2) bulk delete query:</strong> Order 생성시, User가 가진 장바구니는 비워져야합니다. 장바구니는 임시데이터이므로, 크지 않다고 생각했기에 bulk 연산으로 한번에 제거했습니다.
  </p>
  <p>
    <a href = https://github.com/steadykyu/modudogcat_refactoring/blob/cb9746bb7f0c301a038000eb4ce9e100cc6fbbee/src/main/java/com/k5/modudogcat/domain/cart/repository/CartProductRepository.java#L18-L20> CartProductRepository</a> 에서 JPQL Query를 직접 생성하여 DB에 실행시킨 후, 영속성 컨텍스트를 초기화시켰습니다.
  </p>
  <p>
    <strong>3) single select fetch join query:</strong> 모든 엔티티들은 기본적으로 지연로딩을 통해 연관관계를 맺고 있습니다. 하지만 로그인 기능을 위해 User Entity를 사용할때, Role 권한이 필요합니다. 이를 위해 명시적으로 즉시 로딩 될수 있도록 <a href = https://github.com/steadykyu/modudogcat_refactoring/blob/cb9746bb7f0c301a038000eb4ce9e100cc6fbbee/src/main/java/com/k5/modudogcat/domain/user/repository/UserRepository.java#L14-L15>UserRepository</a> 와 같이 fetch join을 사용했습니다.
  </p>
  <p>
    <strong> 4) Batch insert query:</strong> JPA의 Hibernate는 기본적으로 single insert query로 데이터를 삽입합니다. 그러므로 만약 대용량 데이터이고 1:N 관계일때는 N+1개의 insert문을 사용해야하고 이는 성능상 좋지 않을 것이라 생각되었습니다.
  </p>
  <p>
    기존 엔티티 생성전략인 GenerationType.IDENTITY 을 유지하고, Batch Process를 구현하기 위해 <a href=https://github.com/steadykyu/modudogcat_refactoring/blob/cb9746bb7f0c301a038000eb4ce9e100cc6fbbee/src/main/java/com/k5/modudogcat/domain/order/repository/OrderProductRepositoryCustomImpl.java#L18-L45>OrderProductRepositoryCustomImpl</a> Spring Data JPA를 확장시키고, JDBC Template을 사용했습니다. 
  </p>

 ---
 
</details>

<details>
  <summary>3. AWS S3, EC2, RDS를 이용한 3-tier Architecture 기반의 서비스 구현</summary>
  <p>
    <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/studySample/aws_architecture.png" alt="AWS 아키텍처">
  </p>

 ---
 
</details>

<details>
  <summary>4. Spring Security를 이용한 로그인 기능 개발</summary>

---

</details>

</blockquote>

### 트러블 슈팅 경험
**중요 트러블 슈팅** </br>
<blockquote>
<details>
 <summary>1. Spring Security 인증 과정에서 영속성 컨텍스트는 주로 UserDetailsService가 사용자 정보를 조회할 때 사용되지만, 사용자 정보가 반환된 후의 인증 과정에서는 영속성 컨텍스트를 벗어난다.</summary> <br>
	
 <strong>이슈 정의</strong>
 
 <blockquote>
 회원(USER)의 조회 쿼리에서 ROLE 정보가 조회되지 않게 하고 싶었다. 그래서 권한 정보(Role)을 Lazy Loading으로 설정했는데, 로그인 기능에서LazyinitializationException 이 발생한다.
 </blockquote>
 
 <strong>사실 수집</strong>

 <blockquote>
 <details>
 <summary>User 엔티티</summary>
 
```java
package com.k5.modudogcat.domain.user.entity;

@Entity(name = "user_table")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    ...
    @ElementCollection(fetch = FetchType.EAGER) // 로그인을 위해 Eager
    private List<String> roles = new ArrayList<>();
    	 
 ```
User 와 Role 엔티티는 1:N의 관계이다. 따로 엔티티 전용 클래스를 생성하지 않고 위처럼     @ElementCollection 애노테이션을 활용하였다.
 </details>
 
 <details>
 <summary>로그인 기능시 에러 로그</summary>

 ```java
org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.k5.modudogcat.domain.user.entity.User.roles, could not initialize proxy - no Session
	at org.hibernate.collection.internal.AbstractPersistentCollection.throwLazyInitializationException(AbstractPersistentCollection.java:614) ~[hibernate-core-5.6.15.Final.jar:5.6.15.Final]
	at org.hibernate.collection.internal.AbstractPersistentCollection.withTemporarySessionIfNeeded(AbstractPersistentCollection.java:218) ~[hibernate-core-5.6.15.Final.jar:5.6.15.Final]
	at org.hibernate.collection.internal.AbstractPersistentCollection.initialize(AbstractPersistentCollection.java:591) ~[hibernate-core-5.6.15.Final.jar:5.6.15.Final]
	at org.hibernate.collection.internal.AbstractPersistentCollection.read(AbstractPersistentCollection.java:149) ~[hibernate-core-5.6.15.Final.jar:5.6.15.Final]
	at org.hibernate.collection.internal.PersistentBag.iterator(PersistentBag.java:387) ~[hibernate-core-5.6.15.Final.jar:5.6.15.Final]
	at java.base/java.util.Spliterators$IteratorSpliterator.estimateSize(Spliterators.java:1821) ~[na:na]
	at java.base/java.util.Spliterator.getExactSizeIfKnown(Spliterator.java:408) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:483) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474) ~[na:na]
	at java.base/java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:913) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:578) ~[na:na]
	at com.k5.modudogcat.security.util.CustomAuthorityUtils.createAuthorities(CustomAuthorityUtils.java:26) ~[main/:na]
	at com.k5.modudogcat.security.userdetails.UserDetailsServiceImpl$UserDetailsImpl.getAuthorities(UserDetailsServiceImpl.java:41) ~[main/:na]
 ```
 </details>
</blockquote>

 <strong>원인 추론</strong>
 <blockquote>
 <details>
	 <summary>UserDetailService 와 UserDetail은 영속성 컨테이너의 범위에 속하지 않을 수 있다.</summary>

`UserDetailsService`

```java
@RequiredArgsConstructor
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;
    private final CustomAuthorityUtils customAuthorityUtils;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User verifiedUserByLoginId = userService.findVerifiedUserByLoginId(username);

        return new UserDetailsImpl(verifiedUserByLoginId);
    }
```
`UserDetailsService`는 User엔티티의 정보를 받아온 후, 검증에 사용될 `UserDetails` 를 생성하기 위한 인터페이스이다.

위 과정은`UserDetailsServiceImpl.loadUserByUsername()` 구현체의 메서드를 통해 일어나며, 

생성된 `UserDetailsImpl` 는 Security framework에 따라 로그인 기능을 동작하는 과정에서 User 정보와 권한 정보(Role) 를 사용한다.

현재까지 생성된 `UserDetailsImpl`에 User의 정보가 들어가는데 이때 권한 정보(Role)는 지연로딩이므로 들어가지 않는다. 일반적으로 생각했을때, 이후 검증 단계에서 User의 Role을 사용하면 지연로딩이 동작하여 Role 조회쿼리가 동작할 것이라고 생각할 수 있다.

<img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/studySample/security_userDetails.png" alt="userDetail 생성과정">

하지만 보통 `UserDetailsImpl` 생성 이후, 데이터베이스와의 상호작용이 없으므로 위 그림의 `(7)` 이후로는 영속성 컨텍스트의 범위에서 제외되고 그렇기 때문에 지연로딩(Lazy loading)이 동작하지 않아 Role 정보를 가져올 수 없게 된다. 그 결과로 `LazyinitializationException`이 발생하는 것이다.

 </details>
 </blockquote>
 
 <strong>조치 방안 검토</strong>

 <blockquote>
 <details>
	 <summary>UserDetailImpl 생성 과정에 fetch join을 통해 ROLE 정보를 넣어주자.</summary>

  핵심적으로 영속성 컨테이너의 범위에 있는  `loadUserByUsername()`에서 User 정보를 가져오는 `verifiedUserByLoginId` 에서 Fetch join을 통해 Role 정보가 담기도록 쿼리가 동작하도록 만들면 된다.

`loadUserByUsername`

```java
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User verifiedUserByLoginId = userService.findVerifiedUserByLoginId(username);

        return new UserDetailsImpl(verifiedUserByLoginId);
    }
```

`userService.findVerifiedUserByLoginId()`

```java
    public User findVerifiedUserByLoginId(String loginId){
        User findUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
                });

        verifiedActiveUser(findUser);
        return findUser;
    }
```
Repository 계층의 findByLoginId()를 호출한다.

`UserRepository.findByLoginId()`

```java
public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByLoginId(String LoginId);
    
    @Query("select u from user_table u join fetch u.roles where u.loginId = :loginId")
    Optional<User> findByLoginId(@Param("loginId") String LoginId);
```
이전에는 Spring data JPA를 통해 쿼리메소드를 통해 자동으로 쿼리를 생성하고 있었다.

하지만 이제 fetch Join을 통해 명시적으로 권한 정보를 조회시켰다.

 </details>
 </blockquote>

 <strong>결과 관찰</strong>

 <blockquote>
 <details>
	 <summary>로그인 기능시 쿼리 로그</summary>
	 
```java
    select
        user0_.user_id as user_id1_11_,
        user0_.created_at as created_2_11_,
        user0_.modified_at as modified3_11_,
        user0_.address as address4_11_,
        user0_.admin_id as admin_i10_11_,
        user0_.email as email5_11_,
        user0_.login_id as login_id6_11_,
        user0_.name as name7_11_,
        user0_.password as password8_11_,
        user0_.seller_id as seller_11_11_,
        user0_.user_status as user_sta9_11_,
        roles1_.user_table_user_id as user_tab1_12_0__,
        roles1_.roles as roles2_12_0__ 
    from
        user_table user0_ 
    inner join
        user_table_roles roles1_ 
            on user0_.user_id=roles1_.user_table_user_id 
    where
        user0_.login_id=?
```
 </details>
 </blockquote>

 ---
 	
</details>

<details>
	<summary>2. 주문 생성시 쿼리 최적화 (N+1 해결 및 select, insert, delete 쿼리 성능 최적화)</summary> <br>
	
 <strong>이슈 정의</strong>

 <blockquote>
 여러 도메인(User, Product등)이 섞인 주문(Order) 생성을 하는 과정에서 예상보다 많은 쿼리들이 발생하여 성능 최적화의 필요성을 느꼈다.
 </blockquote>
 
 <strong>사실 수집</strong>

 <blockquote>

  <blockquote>

  <strong>주문 생성 기본 로직</strong>

 <details>
	 <summary>주문 생성 메서드</summary>

```java
public Order createOrder(Order order){
        // 영속성 회원 엔티티 넣어주기
        User findUser = userService.findVerifiedUserIncludeCart(order.getUser().getUserId());
        order.setUserAddOrder(findUser);
        
        // 장바구니 비워주기
        emptyCart(findUser.getCart());

        // 요청된 주문속의 상품정보를 통해 Order 엔티티 생성하기
        List<OrderProduct> orderProducts = order.getOrderProductList().stream()
                .map(orderProduct -> {
                    Product findProduct = productService.findProduct(orderProduct.getProduct().getProductId());
                    orderProduct.setProduct(findProduct);
                    return orderProduct;
                })
                .collect(Collectors.toList());
        
        order.setOrderProductList(orderProducts);

				// 재고 감소시키기
        stockMinusCount(order);
        
        Order savedOrder = orderRepository.save(order);
        return savedOrder;
    }
```
 </details>

 <details>
	 <summary>Order 관련 엔티티 소스 코드</summary> <br>

  <details>
	  <summary>USER</summary>
	  
```java
@Entity(name = "user_table")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(length = 20, nullable = false, unique = true)
    private String loginId;
    @Column(length = 20, nullable = false)
    private String name;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String address;
    @Enumerated(value = EnumType.STRING)
    private UserStatus userStatus = UserStatus.USER_ACTIVE;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> roles = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Order> orderList;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "admin_id")
    private Admin admin;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Cart> cart = new ArrayList<>();
    
    public enum UserStatus {
        USER_ACTIVE("활동중"),
        USER_SLEEP("휴면계정"),
        USER_DELETE("삭제된계정");
        @Getter
        private final String status;
        UserStatus(String status){
            this.status = status;
        }
    }

    /**
     * DTO 교환을 위한 생성자
     */
    public User(String loginId, String name, String password, String email, String address) {
        this.loginId = loginId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.address = address;
    }

    /**
     * 연관관계 편의 메서드
     */
    public void addCart(Cart cart){
        this.cart.add(cart);
        cart.setUser(this);
    }

}
```
 </details>
 
 <details>
 <summary><b>Order</b></summary>

```java
@Entity(name = "order_table")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Order extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @ManyToOne
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
    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
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
}
```
 </details>

 <details>
	 <summary>OrderProduct</summary>
	 
```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderProduct extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderProductId;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private Long productCount = 1L;
    @Column(nullable = true)
    private String parcelNumber;
    @Enumerated(value = EnumType.STRING)
    private OrderProductStatus orderProductStatus = OrderProductStatus.ORDER_PAY_STANDBY;

    public enum OrderProductStatus{
        ORDER_PAY_STANDBY("결제대기"),
        ORDER_PAY_FINISH("결제완료"),
        DELIVERY_PREPARE("베송 준비 중"),
        DELIVERY_ING("배송 중"),
        DELIVERY_COMPLETE("배송 완료");
        @Getter
        private final String status;
        OrderProductStatus(String status){
            this.status = status;
        }
    }
}
```
 </details>

 <details>
	 <summary>Product</summary>
	 
```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Product extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    // todo: seller와 연관관계 매핑
    private String name;
    @Lob
    private byte[] thumbnailImage;
    private String thumbnailImageType;
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<ProductDetailImage> productDetailImages = new ArrayList<>();
    @Lob
    private String productDetail;
    private Long price;
    private Long stock;
    @Enumerated(value = EnumType.STRING)
    private ProductStatus productStatus = ProductStatus.PRODUCT_ACTIVE;
    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<OrderProduct> orderProductList = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    public enum ProductStatus {
        PRODUCT_ACTIVE("판매중"),
        PRODUCT_SOLD_OUT("품절"),
        PRODUCT_DELETE("삭제된상품");
        @Getter
        private String status;
        ProductStatus(String status){
            this.status = status;
        }
    }

    public void addProductDetailImages(ProductDetailImage pdi){
        this.productDetailImages.add(pdi);
        if(pdi != null){
            pdi.setProduct(this);
        }
    }
}
```
 </details>
   
 </details>

User는 여러 종류의 Product들을 골라 장바구니에 담는다. 장바구니(Cart)에 담은 각 상품들은 개수를 지정하여 Order를 생성할 수 있다. 이 과정에서 Order 속 Product의 주문량 만큼 Product의 재고량이 빠진다.
  
 </blockquote>

<blockquote>

<strong> 주문 생성 과정중 비즈니스 로직과 쿼리최적화를 고려했을때 아래 5가지의 이슈가 존재 </strong>

아래 소스코드를 살펴보면 주문 생성시 여러 엔티티가 연관되어 있기는 하지만, 예상보다 많은 쿼리가 발생한다.

 <details>
	 <summary>Problem (1) 회원, 장바구니, 장바구니 속 세부 상품 쿼리가 따로따로 조회되고 있다.</summary> <br>

```java
select
        user0_.user_id as user_id1_11_0_,
        user0_.created_at as created_2_11_0_,
        user0_.modified_at as modified3_11_0_,
        user0_.address as address4_11_0_,
        user0_.admin_id as admin_i10_11_0_,
        user0_.email as email5_11_0_,
        user0_.login_id as login_id6_11_0_,
        user0_.name as name7_11_0_,
        user0_.password as password8_11_0_,
        user0_.seller_id as seller_11_11_0_,
        user0_.user_status as user_sta9_11_0_ 
    from
        user_table user0_ 
    where
        user0_.user_id=?
2024-04-10 14:11:40.703 DEBUG 9972 --- [nio-8080-exec-5] org.hibernate.SQL                        : 
    select
        cart0_.cart_id as cart_id1_2_,
        cart0_.user_id as user_id2_2_ 
    from
        cart cart0_ 
    left outer join
        user_table user1_ 
            on cart0_.user_id=user1_.user_id 
    where
        user1_.user_id=?
2024-04-10 14:11:40.714 DEBUG 9972 --- [nio-8080-exec-5] org.hibernate.SQL                        : 
    select
        cartproduc0_.cart_product_id as cart_pro1_3_,
        cartproduc0_.cart_id as cart_id3_3_,
        cartproduc0_.product_id as product_4_3_,
        cartproduc0_.product_count as product_2_3_ 
    from
        cart_product cartproduc0_ 
    left outer join
        cart cart1_ 
            on cartproduc0_.cart_id=cart1_.cart_id 
    where
        cart1_.cart_id=?
```
 </details>

 <details>
	 <summary>Problem (2) 상품 개수만큼 상품을 조회하는 쿼리가 나온다.</summary> <br>

```java
select
        product0_.product_id as product_1_6_0_,
        product0_.created_at as created_2_6_0_,
        product0_.modified_at as modified3_6_0_,
        product0_.name as name4_6_0_,
        product0_.price as price5_6_0_,
        product0_.product_detail as product_6_6_0_,
        product0_.product_status as product_7_6_0_,
        product0_.seller_id as seller_11_6_0_,
        product0_.stock as stock8_6_0_,
        product0_.thumbnail_image as thumbnai9_6_0_,
        product0_.thumbnail_image_type as thumbna10_6_0_ 
    from
        product product0_ 
    where
        product0_.product_id=?
// 주문생성시 A상품을 N개 주문시 N번의 쿼리가 조회된다.
```
 </details>

 <details>
	 <summary>Problem (3) 상품의 개수만큼 Insert 쿼리가 발생함.</summary> <br>

```java
    insert 
    into
        order_product
        (created_at, modified_at, order_id, order_product_status, parcel_number, product_id, product_count) 
    values
        (?, ?, ?, ?, ?, ?, ?)
// 상품의 개수(P) 만큼 insert query가 발생
```
 </details>

 <details>
 <summary>Problem (4) 주문 속 상품 종류의 수만큼 재고 변경의 update문이 동작함</summary> <br>

```java
    update
        product 
    set
        modified_at=?,
        name=?,
        price=?,
        product_detail=?,
        product_status=?,
        seller_id=?,
        stock=?,
        thumbnail_image=?,
        thumbnail_image_type=? 
    where
        product_id=?
// 상품의 개수(K) 만큼 K개의 update 쿼리가 발생
```
 </details>

 <details>
	 <summary>Problem (5) 장바구니 delete쿼리가 장바구니 상세품목 개수만큼 나간다.</summary>

```java
    delete 
    from
        cart_product 
    where
        cart_product_id=?
// 장바구니 속 세부 상품 종류의 수만큼 delete쿼리가 발생
```
 </details>

  </blockquote>
 
 </blockquote>
 
 <strong>원인 추론</strong>
 
 <blockquote>

  <blockquote>
 <strong>Problem (1) : 주문 생성 로직 속의 회원, 장바구니, 세부품목은 1:N:N의 관계인데, 각각 Lazy Loading으로 설정되어 있다.</strong>
  </blockquote>

  <blockquote>
 <strong>Problem (2): N:1 의 관계인 Orderproduct(조인 테이블, N)에 해당하는 Product(1의 관계)를 단건 조회하는 과정에서 N+1 문제가 발생한다.</strong>

 <details>
 	<summary>주문 생성 로직 코드</summary>

```java
public Order createOrder(Order order){
        // 영속성 회원 엔티티 넣어주기
        User findUser = userService.findVerifiedUserIncludeCart(order.getUser().getUserId());
        order.setUserAddOrder(findUser);
        // 장바구니 비워주기
        emptyCart(findUser.getCart());

        // 주문 생성 코드
        List<OrderProduct> orderProducts = order.getOrderProductList().stream()
                .map(orderProduct -> {
//@@@@@@@@@@@@@@@@@@@@@@@@@ (1) Product를 단건 조회하는 findProduct 
                    Product findProduct = productService.findProduct(orderProduct.getProduct().getProductId());
                    orderProduct.setProduct(findProduct);
                    return orderProduct;
                })
                .collect(Collectors.toList());
       
        order.setOrderProductList(orderProducts);

        stockMinusCount(order);
        Order savedOrder = orderRepository.save(order);
        return savedOrder;
    }
```
(1) 위 메서드는 주문 속의 Product의 종류만큼 DB에 단건 조회 쿼리를 생성시킨다. 개발자가 명시적으로 N+1 문제가 일어나도록 설정해버린 코드인 것이다.
 </details>
  </blockquote>
  
 <blockquote>
<strong>Problem (3), (4), (5) : 영속성 컨테이너의 Dirty checking 으로 인한 중복 쿼리 발생</strong>

 Dirty checking으로 동작하는 영속성 컨테이너는 기본적으로 update, delete, insert를 단건으로 처리하므로  N개의 중복 쿼리가 발생한다.

(만약 Order에서 수백만개의 Product을 등록해서 저장한다고 가정했을때, 수백만개의 OrderProduct가 생성되어야한다. 이때 N관계의 OrderProduct를 저장하기 위해 수백만개의 단건 insert 쿼리가 동작할 것이고, 이는 성능 저하로 이어질 것이다.)

  </blockquote>

</blockquote>

 <strong>조치 방안 검토</strong>

<blockquote>

  <blockquote>
 
<strong>Problem (1) : fetch join을 활용하여 필요한 부분을 한번에 모두 조회해버린다.</strong>

 **`1:N` 의 `회원: 장바구니` 는  fetch join을 활용하여 1(회원)을 조회할때 N(장바구니종류)을 한번에 조회해버리자.** 그리고 장바구니세부품목(CartProduct)는 요청시 해당 정보가 제공되므로 조회 대상에서 제외한다. 
 
 이를 활용하면 현재는 미구현이지만, 회원이 여러 종류의 장바구니를 가지는 경우 모든 장바구니를 한번에 조회해올 수 있다. 
 
 <details>
	<summary>fetch join 소스코드</summary>

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u join fetch u.cart where u.userId = :userId")
    Optional<User> findByIdIncludeCart(@Param("userId") Long userId);
```
 </details>

 <details>
	<summary>수정후 쿼리</summary>

```java
    select
        user0_.user_id as user_id1_12_0_,
        cart1_.cart_id as cart_id1_2_1_,
        user0_.created_at as created_2_12_0_,
        user0_.modified_at as modified3_12_0_,
        user0_.address as address4_12_0_,
        user0_.admin_id as admin_i10_12_0_,
        user0_.email as email5_12_0_,
        user0_.login_id as login_id6_12_0_,
        user0_.name as name7_12_0_,
        user0_.password as password8_12_0_,
        user0_.seller_id as seller_11_12_0_,
        user0_.user_status as user_sta9_12_0_,
        cart1_.user_id as user_id2_2_1_,
        cart1_.user_id as user_id2_2_0__,
        cart1_.cart_id as cart_id1_2_0__ 
    from
        user_table user0_ 
    inner join
        cart cart1_ 
            on user0_.user_id=cart1_.user_id 
    where
        user0_.user_id=?
```
 </details>

 </blockquote>

<blockquote>
	
<strong>Problem (2) : 쿼리메소드 IN 을 활용하여 요청한 주문 속의 Product를 모두 가져온다.</strong>

비즈니스 로직상, 요청메시지를 통해 주문 속의 여러 Product의 Id들을 알 수 있다. 그 Id들을 에 해당하는 Product를 in절을 통해 한번에 조회하고, OrderProduct에 적절히 매핑하고 Order를 생성해주자.

 <details>
	<summary>IN 절 쿼리가 생성되도록 수정</summary>

`createOrder()` 로직

```java
public Order createOrder(Order order){
        // 영속성 회원 엔티티 넣어주기
        User findUser = userService.findVerifiedUserIncludeCart(order.getUser().getUserId());
        order.setUserAddOrder(findUser);
        // 장바구니 비워주기
        emptyCart(findUser.getCart());

        // 주문 상품 생성을 위한 상품 조회
        List<Long> productIds = order.getOrderProductList().stream()
                .map(orderProduct -> orderProduct.getProduct().getProductId())
                .collect(Collectors.toList());
// @@@@@@@@(1) 메서드 변경
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
```

`productService.findProductsIds`

```java
    public List<Product> findProductsIds(List<Long> productIds){
        List<Product> productsByIdIn = productRepository.findProductsByProductIdIn(productIds);
        return productsByIdIn;
    } 
```

`productRepository.findProductsByProductIdIn`
:Spring Data JPA를 통해 IN 절을 활용하는 쿼리메서드 생성**

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findProductsByProductIdIn(List<Long> productIds);
}
```
 </details>

  </blockquote>

  <blockquote>

<strong>Problem (3), (4): N insert, update 부분을 JDBC Template의 batch Process로 처리한다.</strong>

기본적으로 Hibernate 기반의 JPA 기술들은(변경감지, Spring Data JPA) 단일 query로 처리 한다. 이를 막기위해 JPA 기술로 배치 프로세싱을 적용하는 것은 수많은 작업이 들어간다. 차라리 ORM 프레임워크를 조금 포기하더라도, DB에 batch query 를 직접 날리도록 지시하여 더 쉽게 쿼리 성능 최적화를 이룰 수 있었다.
(`(3)`,`(4)` 해결방안이 같으므로 여기서는 (3)의 해결 모습만 다루도록 한다.)

 <details>
	 <summary>insert 중복 쿼리 문제 해결</summary>

사실 주문 속 상품정도는 대용량 데이터라고 보기 힘들지만, 미래 대용량 데이터를 다룰 일이 있을 것 같아서 가정해보자. 해당 이슈는 JDBC Template이 사용가능한 커스텀 Repository를 생성함으로써 Batch insert를 가능하도록 만들었다.

 <details>
	 <summary>커스텀 Repository 를 통한 JDBC Templete 사용</summary>

`OrderProductRepositoryCustom` (커스텀 Repository interface)

```java
package com.k5.modudogcat.domain.order.repository;

import com.k5.modudogcat.domain.order.entity.OrderProduct;

import java.util.List;

public interface OrderProductRepositoryCustom {
    public List<OrderProduct> batchSaveOrderProducts(List<OrderProduct> orderProducts, Long orderId);
}
```

커스텀 Repository 구현체

```java
@RequiredArgsConstructor
public class OrderProductRepositoryCustomImpl implements OrderProductRepositoryCustom {
    private final JdbcTemplate jdbcTemplate;

    public List<OrderProduct> batchSaveOrderProducts(List<OrderProduct> orderProducts, Long orderId) {
        // Identity 전략 - pk 제외
        // 나머지 전략시 - pk 포함
        String sql = "INSERT INTO order_product (order_product_status, parcel_number, product_count, order_id, product_id, created_at, modified_at)" +
                " VALUES (?, ?, ?, ?, ? ,? ,?)";
        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // i번째 객체를 가져온 후, DB에 삽입한다.
                OrderProduct orderProduct = orderProducts.get(i);
                ps.setString(1, orderProduct.getOrderProductStatus().name());
                ps.setString(2, orderProduct.getParcelNumber());
                ps.setLong(3, orderProduct.getProductCount());
                ps.setLong(4, orderId);
                ps.setLong(5, orderProduct.getProduct().getProductId());
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(6, timestamp);
                ps.setTimestamp(7, timestamp);

                orderProduct.setCreatedAt(LocalDateTime.now());
                orderProduct.setModifiedAt(LocalDateTime.now());
            }

            public int getBatchSize() {
                return orderProducts.size();
            }
        });

        return orderProducts;
    }
}

```

`OrderProductRepository`-확장 완료

```java
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> , OrderProductRepositoryCustom{
    Page<OrderProduct> findByProductSellerSellerId(Long sellerId, Pageable pageable);
}
```
Spring Data JPA 와 OrderProductRepositoryCustomImpl 메서드 사용이 가능

 </details>

 <details>
	 <summary>커스텀 레포지토리 사용 및 Order 수정</summary>

```java
public Order createOrder(Order order){
        //... 주문 생성 로직

        // @@@@@@@@@@@@(1)JDBC 저장 방식
        List<OrderProduct> orderProducts = orderProductRepository.batchSaveOrderProducts(order.getOrderProductList(), savedOrder.getOrderId());
        savedOrder.setOrderProductList(orderProducts);
        return savedOrder;
    }
```
영속성 컨텍스트의 관리를 받도록 JPA로 Order를 저장후, OrderId를 가져온다. 해당 id를 이용하여 JDBC Templates의 Batch insert 쿼리를 생성시킨다.
 
`OrderProduct`

```java
  @OneToMany(mappedBy = "order", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
  @BatchSize(size = 12)
  private List<OrderProduct> orderProductList = new ArrayList<>();
```
Order 저장시, JPA로 인해 OrderProduct가 저장되지 않도록 Cascade.Persist를 제거시켰다.

 </details>

 <details>
	 <summary>수정 후, 결과 쿼리</summary>

JPA 에서 동작하지 않기에 중복된 단건 쿼리를 출력하지 않는다. 대신 배치 처리를 통해 아래의 방식으로 쿼리가 동작한다.

```java
INSERT INTO table1 (col1, col2) VALUES
(val11, val12),
(val21, val22),
(val31, val32);
```
 </details>
 
 </details>

  </blockquote>

  <blockquote>
<strong>Problem (5) : 일괄 처리로 되는 경우, batch process 대신 @Modifying 의 bulk 연산으로 처리한다.</strong>

비즈니스 로직상 주문 상품시 회원이 가진 장바구니의 장바구니 세부품목을 초기화해야한다. 

`Problem(1)`에서 회원과 장바구니를 fetch join으로 조회했으므로, 우리는 회원이 가진 장바구니들을 알 수 있고, 그 장바구니들의 ID를 이용할 수 있다. 

`Problem(3)`의 insert의 경우, 어느 정보들을 넣어주어야하는지 알 수 없었지만 delete 경우는 삭제 적용 대상을 알 수 있고, 삭제라는 일괄적으로 같은 연산을 적용한다. 이런 경우, 굳이 JDBC를 쓰기보다는 JPA의 bulk 연산을 통해 일괄 처리가 가능하다.

 <details>
	<summary>bulk 연산을 Repository에 추가후 적용</summary>

`cartProductRepository.deleteAllByCartIds()`
:@Modifying을 통해 bulk delete 처리를 해주었다.

```java
@Modifying(clearAutomatically = true) // em.clear()
@Query("delete CartProduct cp where cp.cart.cartId in :cartIds")
void deleteAllByCartIds(@Param("cartIds") List<Long> cartIds);
```

`CartService.removeCartProductsByCarts()`
```java
public void removeCartProductsByCarts(List<Cart> carts){
        List<Long> cartIds = carts.stream()
                .map(cart -> cart.getCartId())
                .collect(Collectors.toList());
//@@@@(1)        cartProductRepository.deleteAllByCartCartIdIn(cartIds);
        cartProductRepository.deleteAllByCartIds(cartIds);
    }
```
(1) 쿼리메소드가 아닌 bulk 연산의 메소드를 적용시킨다.

 </details>
   
   <blockquote>
 
   </blockquote>
 
</details>

</blockquote>

<details>
<summary><b>그 외 트러블 슈팅</b></summary><br>

<blockquote>
	
<details>
  <summary>1. Update 기능 리팩토링하기(merge 방식 → DirtyChecking 방식) </summary> <br>
  <strong>문제정의</strong>

  <blockquote>
	  
  이전 수정 기능의 코드들을 살펴보겠습니다.
  
  ```java
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

        return userRepository.save(findUser); //---------------- (1)
    }
  ```

  (1): 디버그를 찍어보면 JpaRepository의 구현체인 SimpleJpaRepository의 save()를 사용한다. 이 방식은 merge() 인 병합의 방식을 사용하는데, 이는 병합에 사용하는 엔티티 객체의 모든 필드를 가져와서
  병합을 시도하므로, 만약 수정되려는 해당 객체의 일부 필드에 null이 존재하는 경우 수정전 필드의 값을 null로 수정할 수 있습니다. 즉 필드 누락의 가능성이 존재합니다.

  </blockquote>

  <strong>제안하는 방안</strong>

  <blockquote>
  
병합보다 JPA에서 권장하는 변경 감지 방식을 이용하면 수정하지 않는 엔티티 필드값은 유지하고, 수정된 필드의 값만 변경하여 update 쿼리를 날리도록 동작한다.
  - 필드 누락 가능성 감소
  - 효율적인 SQL 생성
  - 데이터베이스 통신 최소화
  - 캐시 이점 활용

</blockquote>

  <strong> 문제 해결</strong>

<blockquote>
  
  ```java
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
  ```
  JPA가 권장하는 변경감지로 동작하도록 명시적으로 save()를 사용하지 않고, 엔티티를 그대로 반환했다. 이제 엔티티는 캐시에 저장된 수정 전의 엔티티와 비교하여 변경감지의 대상이 되어 수정된 필드의 값만 변경시킨 적절한 update 쿼리를 날려준다.

</blockquote>
  
<strong>결과 관찰</strong>

<blockquote>
	
```java
Hibernate: 
    update
        user_table 
    set
        modified_at=?,
        address=?,
        admin_id=?,
        email=?,
        login_id=?,
        name=?,
        password=?,
        seller_id=?,
        user_status=? 
    where
        user_id=?
```

---

</blockquote>

</details>

<details>
  <summary>2. 페이징시 AWS 서버 프리징 이슈 - 물리적 해결 </summary> <br>
	
  <strong>이슈 정의</strong>

  <blockquote>
  
프로젝트의 홈페이지는 전체상품을 페이징하여 가져오는 작업이다. 해당 작업시 CPU를 비정상적으로 사용하는 모습을 발견했다. CPU가 비정상적으로 사용되면서, AWS 무료 크레딧이 모두 소진되어 서버가 다운되는 현상이 일어났다.

 </blockquote>

  <strong>원인 추론</strong>

  <blockquote>
AWS 프리티어에서 제공하는 t2.micro(CPU)는 CPU 크레딧이 라는 개념이 존재한다. 해당 baseline 이라는 허용하는 사용치를 초과해서 사용하면 CPU 성능 제공을 멈춘다. 이러한 이유로 서버가 멈추는 것임을 알았다.
  </blockquote>

  <strong>조치 방안 검토</strong>

  <blockquote>
  
  (1) 방안: swap 메모리로 RAM 성능을 끌어올리자.
  취준생 신분으로 돈으로 리소스를 늘릴 수는 없어서, SWAP메모리를 통해 EC2의 HDD 리소스를 일부 RAM으로 활용하는 전략을 택했다.
  
  <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/studySample/swap메모리적용후_CPU사용율.png" alt="swap 메모리후 CPU">
  
  페이지네이션 CPU 사용률이 올라가는 모습을 볼 수 있는데, CPU 사용량이 8퍼센트 정도로 서버가 터질정도로 문제가 되지 않는 모습을 볼 수 있다.

  - 19:00 : 여러 User들의 요청을 페이지네이션

  </blockquote>
  
  <strong>결과 관찰</strong>

  <blockquote>
  
결과적으로 이제 서버가 터지지 않고 Spring 프로젝트를 빌드할 수 있고, 페이지네이션에도 문제가 발생하지 않았다.

---

  </blockquote>

</details>

<details>
  <summary>3. 페이징시 AWS 프리징 이슈 해결 - select query 성능 최적화 하기</summary> <br>
	
  <strong>이슈 정의</strong>

  <blockquote>
  홈페이지가 열릴때는 여러 Product 를 조회하도록 페이지네이션 기능이 동작한다. 그런데 해당 페이지를 열때 많은 시간이 걸리는 것을 발견했다.
  </blockquote>
  
  <strong>사실 수집 및 원인 추론</strong>
  
  <blockquote>
  
이전 해당 코드를 작성한 백엔드 개발자와의 소통 부족으로 적절하지 않은 정보들이 응답메시지와 로그에 담기고 있었다. 

  <blockquote>
<strong>문제 1) 페이지마다 같은 내용의 중복 쿼리 내용의 로그가 발생하여 성능을 망가트리고 있다.</strong>
  
  <details>
  <summary>문제1 로그</summary>
    
  ```java
        select
        product0_.product_id as product_1_6_,
        product0_.created_at as created_2_6_,
        product0_.modified_at as modified3_6_,
        product0_.name as name4_6_,
        product0_.price as price5_6_,
        product0_.product_detail as product_6_6_,
        product0_.product_status as product_7_6_,
        product0_.seller_id as seller_11_6_,
        product0_.stock as stock8_6_,
        product0_.thumbnail_image as thumbnai9_6_,
        product0_.thumbnail_image_type as thumbna10_6_
    from
        product product0_
    where
        product0_.product_status not like ? escape ?
    order by
        product0_.created_at desc limit ?,
        ?
[Hibernate]
    select
        product0_.product_id as product_1_6_,
        product0_.created_at as created_2_6_,
        product0_.modified_at as modified3_6_,
        product0_.name as name4_6_,
        product0_.price as price5_6_,
        product0_.product_detail as product_6_6_,
        product0_.product_status as product_7_6_,
        product0_.seller_id as seller_11_6_,
        product0_.stock as stock8_6_,
        product0_.thumbnail_image as thumbnai9_6_,
        product0_.thumbnail_image_type as thumbna10_6_
    from
        product product0_
    where
        product0_.product_status not like ? escape ?
    order by
        product0_.created_at desc limit ?,
        ?
  ```
  </details>
  </blockquote>

  <blockquote>
<strong>문제 2) 기능에 필요없는 연관관계의 엔티티를 조회하며 성능을 망가트리고 있다.</strong>
  <details>
  <summary>문제2 로그</summary>
  ```java
  select
        seller0_.seller_id as seller_i1_10_0_,
        seller0_.created_at as created_2_10_0_,
        seller0_.modified_at as modified3_10_0_,
        seller0_.account_number as account_4_10_0_,
        seller0_.address as address5_10_0_,
        seller0_.bank_name as bank_nam6_10_0_,
        seller0_.email as email7_10_0_,
        seller0_.login_id as login_id8_10_0_,
        seller0_.name as name9_10_0_,
        seller0_.password as passwor10_10_0_,
        seller0_.phone as phone11_10_0_,
        seller0_.registration_number as registr12_10_0_,
        seller0_.seller_status as seller_13_10_0_,
        user1_.user_id as user_id1_11_1_,
        user1_.created_at as created_2_11_1_,
        user1_.modified_at as modified3_11_1_,
        user1_.address as address4_11_1_,
        user1_.admin_id as admin_i10_11_1_,
        user1_.email as email5_11_1_,
        user1_.login_id as login_id6_11_1_,
        user1_.name as name7_11_1_,
        user1_.password as password8_11_1_,
        user1_.seller_id as seller_11_11_1_,
        user1_.user_status as user_sta9_11_1_,
        roles2_.user_table_user_id as user_tab1_12_2_,
        roles2_.roles as roles2_12_2_
    from
        seller seller0_
    left outer join
        user_table user1_
            on seller0_.seller_id=user1_.seller_id
    left outer join
        user_table_roles roles2_
            on user1_.user_id=roles2_.user_table_user_id
    where
        seller0_.seller_id=?
[Hibernate]
    select
        seller0_.seller_id as seller_i1_10_0_,
        seller0_.created_at as created_2_10_0_,
        seller0_.modified_at as modified3_10_0_,
        seller0_.account_number as account_4_10_0_,
        seller0_.address as address5_10_0_,
        seller0_.bank_name as bank_nam6_10_0_,
        seller0_.email as email7_10_0_,
        seller0_.login_id as login_id8_10_0_,
        seller0_.name as name9_10_0_,
        seller0_.password as passwor10_10_0_,
        seller0_.phone as phone11_10_0_,
        seller0_.registration_number as registr12_10_0_,
        seller0_.seller_status as seller_13_10_0_,
        user1_.user_id as user_id1_11_1_,
        user1_.created_at as created_2_11_1_,
        user1_.modified_at as modified3_11_1_,
        user1_.address as address4_11_1_,
        user1_.admin_id as admin_i10_11_1_,
        user1_.email as email5_11_1_,
        user1_.login_id as login_id6_11_1_,
        user1_.name as name7_11_1_,
        user1_.password as password8_11_1_,
        user1_.seller_id as seller_11_11_1_,
        user1_.user_status as user_sta9_11_1_,
        roles2_.user_table_user_id as user_tab1_12_2_,
        roles2_.roles as roles2_12_2_
    from
        seller seller0_
    left outer join
        user_table user1_
            on seller0_.seller_id=user1_.seller_id
    left outer join
        user_table_roles roles2_
            on user1_.user_id=roles2_.user_table_user_id
    where
        seller0_.seller_id=?
2024-03-20 09:27:54.097 DEBUG 1188 --- [nio-8080-exec-9] org.hibernate.SQL                        :
    select
        cart0_.cart_id as cart_id1_2_1_,
        cart0_.user_id as user_id2_2_1_,
        user1_.user_id as user_id1_11_0_,
        user1_.created_at as created_2_11_0_,
        user1_.modified_at as modified3_11_0_,
        user1_.address as address4_11_0_,
        user1_.admin_id as admin_i10_11_0_,
        user1_.email as email5_11_0_,
        user1_.login_id as login_id6_11_0_,
        user1_.name as name7_11_0_,
        user1_.password as password8_11_0_,
        user1_.seller_id as seller_11_11_0_,
        user1_.user_status as user_sta9_11_0_,
        roles2_.user_table_user_id as user_tab1_12_3_,
        roles2_.roles as roles2_12_3_
    from
        cart cart0_
    left outer join
        user_table user1_
            on cart0_.user_id=user1_.user_id
    left outer join
        user_table_roles roles2_
            on user1_.user_id=roles2_.user_table_user_id
    where
        cart0_.user_id=?
[Hibernate]
    select
        cart0_.cart_id as cart_id1_2_1_,
        cart0_.user_id as user_id2_2_1_,
        user1_.user_id as user_id1_11_0_,
        user1_.created_at as created_2_11_0_,
        user1_.modified_at as modified3_11_0_,
        user1_.address as address4_11_0_,
        user1_.admin_id as admin_i10_11_0_,
        user1_.email as email5_11_0_,
        user1_.login_id as login_id6_11_0_,
        user1_.name as name7_11_0_,
        user1_.password as password8_11_0_,
        user1_.seller_id as seller_11_11_0_,
        user1_.user_status as user_sta9_11_0_,
        roles2_.user_table_user_id as user_tab1_12_3_,
        roles2_.roles as roles2_12_3_
    from
        cart cart0_
    left outer join
        user_table user1_
            on cart0_.user_id=user1_.user_id
    left outer join
        user_table_roles roles2_
            on user1_.user_id=roles2_.user_table_user_id
    where
        cart0_.user_id=?
  ```
  </details>
  Product를 페이지네이션 기능에는 Product의 필드 정보까지만 가져오면 된다. 그러나 Product와 연관관계가 존재하는 엔티티(판매자, 권한, 유저정보등)의 정보들도 조인을 통해 가져오고 있다.

  -> 연관관계 엔티티간의 CasCade, LazyLoading, EagerLoading을 조사하자.

  </blockquote>

<blockquote>
<strong>문제 3) 기능에 필요없는 연관관계의 엔티티를 조회하며 성능을 망가트리고 있다.</strong>
  <details>
    <summary>문제3 로그</summary>
    ```java
     select
        productdet0_.product_id as product_4_7_0_,
        productdet0_.detail_image_id as detail_i1_7_0_,
        productdet0_.detail_image_id as detail_i1_7_1_,
        productdet0_.image as image2_7_1_,
        productdet0_.product_id as product_4_7_1_,
        productdet0_.type as type3_7_1_
    from
        product_detail_image productdet0_
    where
        productdet0_.product_id=?
        
Size 만큼  N번 반복!!
...
    ```
  </details>
   홈페이지에는 Product의 썸네일 이미지만 조회하면 된다. 그러나 상품 속 디테일 이미지까지 조회하고 있으며, N+1 문제가 발생하고 있다.

  -> 연관관계 엔티티간의 CasCade, LazyLoading, EagerLoading을 조사하자.

</blockquote>

<blockquote>
<strong>문제4) 기능에 필요없는 연관관계의 엔티티를 조회하며 성능을 망가트리고 있다.</strong>

  <details>
    <summary>문제 4 로그</summary>
    ```java
      select
        product0_.product_id as product_1_6_0_,
        product0_.created_at as created_2_6_0_,
        product0_.modified_at as modified3_6_0_,
        product0_.name as name4_6_0_,
        product0_.price as price5_6_0_,
        product0_.product_detail as product_6_6_0_,
        product0_.product_status as product_7_6_0_,
        product0_.seller_id as seller_11_6_0_,
        product0_.stock as stock8_6_0_,
        product0_.thumbnail_image as thumbnai9_6_0_,
        product0_.thumbnail_image_type as thumbna10_6_0_,
        seller1_.seller_id as seller_i1_10_1_,
        seller1_.created_at as created_2_10_1_,
        seller1_.modified_at as modified3_10_1_,
        seller1_.account_number as account_4_10_1_,
        seller1_.address as address5_10_1_,
        seller1_.bank_name as bank_nam6_10_1_,
        seller1_.email as email7_10_1_,
        seller1_.login_id as login_id8_10_1_,
        seller1_.name as name9_10_1_,
        seller1_.password as passwor10_10_1_,
        seller1_.phone as phone11_10_1_,
        seller1_.registration_number as registr12_10_1_,
        seller1_.seller_status as seller_13_10_1_,
        user2_.user_id as user_id1_11_2_,
        user2_.created_at as created_2_11_2_,
        user2_.modified_at as modified3_11_2_,
        user2_.address as address4_11_2_,
        user2_.admin_id as admin_i10_11_2_,
        user2_.email as email5_11_2_,
        user2_.login_id as login_id6_11_2_,
        user2_.name as name7_11_2_,
        user2_.password as password8_11_2_,
        user2_.seller_id as seller_11_11_2_,
        user2_.user_status as user_sta9_11_2_
    from
        product product0_
    left outer join
        seller seller1_
            on product0_.seller_id=seller1_.seller_id
    left outer join
        user_table user2_
            on seller1_.seller_id=user2_.seller_id
    where
        product0_.product_id=?

    + (위 seller와 연관되어있는 User의 상세정보를 Join query로 가져옴)
    ```
  </details>
  
  상품 속 개별 썸네일 파일을 서버에 요청할때, 연관관계의 엔티티인 Product, Seller, UserInfo등 해당 기능에 쓸모 없는 엔티티 정보들이 조인하여 가져오는 쿼리를 날리고 있다.
  
  -> 연관관계 엔티티간의 CasCade, LazyLoading, EagerLoading을 조사하자.

  <blockquote>

  </blockquote>

  <strong> 조치 방안 검토</strong>

  <blockquote>

  <details>
    <summary>문제1 해결: Logging 설정 정보 변경으로 중복 로그 제거하기</summary> <br>
	  
```java
spring:
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        highlight_sql: true
        format_sql: true
        #show_sql: true
--------------------------
logging:
  level:
    org:
      hibernate:
        SQL: DEBUG
        type: DEBUG
```
  </details>

  <details>
    <summary>문제2,3,4 해결: 지연 로딩을 통한 쿼리최적화</summary> <br>
	  
홈페이지에서 조회에 사용되는 Product를 LazyLoading으로 지정하여 필요한 정보만을 담은 적절한 Response를 만들어준다.

```java
package com.k5.modudogcat.domain.product.entity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Product extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String name;
    @Lob
    private byte[] thumbnailImage;
    private String thumbnailImageType;
    
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ (1) 연관 엔티티들에 Lazy 지정하기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    
    ...
}

```

  </details>

  </blockquote>

  <strong> 결과 관찰 </strong>

  <blockquote>

지연로딩을 통해 필요한 정보만 가진 Response를 생성함으로써 조회 성능이 최적화된 아래의 쿼리로 페이징 기능이 동작한다!

  ```java
2024-03-24 18:42:25.391 DEBUG 24548 --- [nio-8080-exec-2] org.hibernate.SQL                        : 
    select
        product0_.product_id as product_1_6_,
        product0_.created_at as created_2_6_,
        product0_.modified_at as modified3_6_,
        product0_.name as name4_6_,
        product0_.price as price5_6_,
        product0_.product_detail as product_6_6_,
        product0_.product_status as product_7_6_,
        product0_.seller_id as seller_11_6_,
        product0_.stock as stock8_6_,
        product0_.thumbnail_image as thumbnai9_6_,
        product0_.thumbnail_image_type as thumbna10_6_ 
    from
        product product0_ 
    where
        product0_.product_status not like ? escape ? 
    order by
        product0_.created_at desc limit ?
2024-03-24 18:42:25.492 DEBUG 24548 --- [nio-8080-exec-2] org.hibernate.SQL                        : 
    select
        count(product0_.product_id) as col_0_0_ 
    from
        product product0_ 
    where
        product0_.product_status not like ? escape ?
  ```
  </blockquote>
  
</details>

<details>
  <summary>4. 양방향 @OneToOne 관계에서 Lazy Loading이 동작하지 않는 이슈 해결</summary> <br>
  <strong>이슈 정의</strong>

  <blockquote>
장바구니(Cart) 엔티티는 회원 엔티티와 일대일 관계이고, 장바구니를 연관관계 주인으로 설정해 두었다. 그런데 @OneToOne 양방향 매핑속에서 주인이 아닌 쪽(여기서는 회원)의 조회 쿼리를 날리는 기능을 동작시키니 장바구니 정보가 필요없음에도 장바구니 엔티티를 지연로딩이 아닌 즉시 로딩을 해오고 있다.

  </blockquote>
  
  <strong>사실 추론</strong>

  <blockquote>
	  
  <details>
    <summary>User 엔티티</summary>
    
```java
@Entity(name = "user_table")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
	   ...
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@(1)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Cart cart;
    
    public enum UserStatus {
        USER_ACTIVE("활동중"),
        USER_SLEEP("휴면계정"),
        USER_DELETE("삭제된계정");
        @Getter
        private final String status;
        UserStatus(String status){
            this.status = status;
        }
    }

    /**
     * 연관관계 편의 메서드
     */
    public void addCart(Cart cart){
        this.cart = cart;
        cart.setUser(this);
    }

}
```
(1) 조회에 사용되는 User 엔티티는 연관관계 주인이 아닌쪽이며 Cart와 양방향 매핑이고, Lazy loading으로 설정되어 있다.
  </details>
  <details>
    <summary>User를 조회하는 쿼리</summary>

```java
    select
        user0_.user_id as user_id1_11_0_,
        user0_.created_at as created_2_11_0_,
        user0_.modified_at as modified3_11_0_,
        user0_.address as address4_11_0_,
        user0_.admin_id as admin_i10_11_0_,
        user0_.email as email5_11_0_,
        user0_.login_id as login_id6_11_0_,
        user0_.name as name7_11_0_,
        user0_.password as password8_11_0_,
        user0_.seller_id as seller_11_11_0_,
        user0_.user_status as user_sta9_11_0_,
        roles1_.user_table_user_id as user_tab1_12_1_,
        roles1_.roles as roles2_12_1_ 
    from
        user_table user0_ 
    left outer join
        user_table_roles roles1_ 
            on user0_.user_id=roles1_.user_table_user_id 
    where
        user0_.user_id=?
//---------------문제의 추가된 장바구니 쿼리------------------
    select
        cart0_.cart_id as cart_id1_2_0_,
        cart0_.user_id as user_id2_2_0_ 
    from
        cart cart0_ 
    where
        cart0_.user_id=?
```
회원을 조회하는 기능에는 당장 장바구니가 필요없어서 Lazy loading으로 엔티티관계를 설정했었다. 그러나 위 쿼리처럼 장바구니가 즉시로딩 되고 있다.
  </details>

  </blockquote>

  <strong>원인 추론</strong>

  <blockquote>
	  
양방향 매핑의 OneToOne 의 경우, 주인이 아닌 엔티티를 조회할때 주인쪽 엔티티의 외래키 필드에 프록시 객체를 넣어야 할지 null을 넣어야할지 JPA가 유추할 수 없다는 이유로 즉시로딩이 동작하도록 설정되어 있었다.
DB 시각으로 봐보면 위 설정의 이유를 알 수 있다. 주인이 아닌 엔티티 테이블 정보로 연관관계 엔티티 테이블의 외래키 필드 값의 존재 여부를 알 수 없다. 그러므로 JPA는 주인쪽 외래키 필드에 null을 넣기도 애매하고 프록시 객체를 만들어두기도 애매하여 즉시로딩을 동작시킨다.

  </blockquote>

<strong>조치 방안 검토</strong>

  <blockquote>

1. 설계 구조를  OneToMany 또는 ManyToOne 관계로 변경하기
2. 장바구니 정보를 페치 조인으로 한개의 쿼리로 전부 조회시키기
3. byte code instrument을 이용

설계상으로 회원은 여러개의 장바구니를 가질 수 있고 관련 추가 기능 개발시 확장성이 가능하도록 하다는 점, 코드 수정에 큰 리소스가 들어가지 않다는 점을 이유로 장바구니와의 연관관계를 @ManyToOne으로 변경하는 1번의 조치방안을 선택했다.

</blockquote>

<strong>결과 적용 후 관찰</strong>

  <blockquote>

<details>
<summary>회원(구매자) 조회 쿼리가 이제 장바구니를 제외하고 한번만 조회한다.</summary>
  
```java
    select
        user0_.user_id as user_id1_11_0_,
        user0_.created_at as created_2_11_0_,
        user0_.modified_at as modified3_11_0_,
        user0_.address as address4_11_0_,
        user0_.admin_id as admin_i10_11_0_,
        user0_.email as email5_11_0_,
        user0_.login_id as login_id6_11_0_,
        user0_.name as name7_11_0_,
        user0_.password as password8_11_0_,
        user0_.seller_id as seller_11_11_0_,
        user0_.user_status as user_sta9_11_0_,
        roles1_.user_table_user_id as user_tab1_12_1_,
        roles1_.roles as roles2_12_1_ 
    from
        user_table user0_ 
    left outer join
        user_table_roles roles1_ 
            on user0_.user_id=roles1_.user_table_user_id 
    where
        user0_.user_id=?
```

---

</blockquote>

</details>

</details>

</blockquote>

</details>


### 회고/피드백
**만족한점** </br>

<blockquote>
	
<details>
  <summary>1. APP 개발 과정 중 발생한 트러블 슈팅 과제들을 해결하여 최종적으로 동작하는 서비스를 개발했다.</summary>
  <p>
    </br>
    인프런 강의 내용이나 검색등을 통해 위의 트러블 슈팅들을 해결하며 최종적으로 쇼핑몰 서비스를 제공하는 웹 사이트를 개발함으로써 뿌듯함을 느꼈습니다.
  </p>
</details>
<details>
  <summary>2. Back과 API 통신을 기반으로 동작하는 SPA(Single Page Application) 관련 개발 경험 및 지식을 얻었다. </summary>
</br>

<strong>처음으로 SPA 방식으로 개발하다보니 발생한 문제</strong> <br>

1. 서버의 입장이다 보니 프론트쪽에서 편하게 데이터를 이용하게하면서 동시에 쿼리를 최적화하려면 어떤 형식으로, 어떤 범위의 데이터를 보내주어야하는지
2. 네트워크 통신과정에서 에러가 발생시, 어느쪽의 에러인지 알수 없는 문제 </br>


<strong> 문제를 해결하며 배운점 </strong> <br>
1. SPA 일때, Web은 서버에서 응답한 데이터를 전역 객체 사용하여 한/여러 페이지에서 재사용이 가능하고 이를 통해 추가적인 네트워크 요청/응답을 줄일 수 있다는 점
2. 네트워크를 통해 동작하는 웹과 WAS 전체적인 아키텍처

</details>

</blockquote>

**아쉬운점** </br>

<blockquote>
	
<details>
  <summary> 1. 팀원들과 깊은 대화를 통해 기획 및 설계하지 못했다.</summary>
  <p>
        데이터베이스 설계나 네트워크, 디자인 패턴등 cs지식이 부족한 과정에서 기획과 설계를 진행하다보니 해답이 나오지 않는 부분에 시간을 너무 많이 허비했습니다. 우선 바로 구현할 수 있는 지식으로 APP을 개발 해두고, 하나씩 추가하거나 리팩토링 하는 방식으로 개발했어야 한다고 생각이 듭니다. 
    </br>
  </br>
  예를 들어 ERD를 설계할 때 구매자, 판매자, 관리자를 어떻게 설계할 것인가로 이야기를 나누었는데, DB 설계 지식이 부족했다보니 무엇이 좋은 ERD 인가 고민하며 시간을 많이 허비했습니다.
    간단하게 모두 도메인으로 만들어두고 기능을 개발하며 하나하나 필요하거나 새로운 기술들을 좀 더 적용해보면 개발 기간동안에 더 많은 것들을 할 수 있지 않았나 생각이 듭니다.
  </p>
  <p>
    또한 깊은 대화를 통해 팀원들의 실력을 객관적으로 나타내보고, 이에 맞게 업무를 배분했으면 중간에 이탈자가 발생하지 않을 것이라고 생각이 듭니다.
  </p>
</details>
<details>
  <summary> 2. 디테일한, 보기좋은 user or business sequence flow를 만들지 못한 부분</summary>
  <p>
    </br>
    디테일하고 프론트, 백 양쪽이 이해할 수 있는 user sequence flow를 만들지 못하다보니, 이후 개발 과정에서 로직 중간과정은 어떻게 진행되는가를 물어보는 과정이 자주 일어났던것 같습니다. 
    차라리 처음에 draw.io같은 사이트를 이용하여 flow를 그림으로 구체적으로 그려놓고 부연설명을 붙여 놓은 후 조금조금씩 수정했다면 시간을 많이 절약할 수 있었을 것이라고 생각됩니다.
  </p>
</details>

<details>
  <summary> 3. CS 지식의 부족함을 느낀점</summary>
  <p>
  Spring JPA를 사용하다보니 결국 데이터베이스에 대한 지식이 필요하고, 프론트/백/DB간 소통 과정에서 네트워크에 대한 지식이 필요하고, AWS나 리눅스를 제대로 사용하기 위해선 운영체제나/인프라의 지식이 필요함을 느꼈습니다. 더 좋은 APP 개발과 기술들을 제대로 활용하기 위해서는 CS 공부가 바탕이 되는구나를 느꼈습니다.
  </p>
</details>

</blockquote>
