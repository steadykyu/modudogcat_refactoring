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
<details>
  <summary>ERD 그림</summary>
  <p>
    <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/ERD.png" alt="AWS 아키텍처">
  </p>
</details>

### 핵심 기능
<details>
  <summary>(1) CSR 아키텍처를 채택하여 RESTful API 호출을 통해 통신하는 쇼핑몰 백엔드 서비스를 설계 및 구현</summary>
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
</details>
<details> 
  <summary>(2) Spring JPA를 이용한 WAS 개발 및 쿼리 성능 최적화</summary>
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
</details>
<details>
  <summary>(3) AWS S3, EC2, RDS를 이용한 3-tier Architecture 기반의 서비스 구현</summary>
  <p>
    <img src="https://github.com/steadykyu/modudogcat_refactoring/blob/main/sampleImage/studySample/aws_architecture.png" alt="AWS 아키텍처">
  </p>
</details>
<details>
  <summary>(4) Spring Security를 이용한 로그인 기능 개발</summary>

</details>


### 트러블 슈팅 경험
**(1) 중요 트러블 슈팅** </br>

**(2) 그 외 트러블 슈팅** </br>
<details>
  <summary>(1) Update 기능 리팩토링하기(merge 방식 → DirtyChecking 방식) </summary>
  <strong>문제정의</strong>
  
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

  <strong>제안하는 방안</strong>
  
  병합보다 JPA에서 권장하는 변경 감지 방식을 이용하면 수정하지 않는 엔티티 필드값은 유지하고, 수정된 필드의 값만 변경하여 update 쿼리를 날리도록 동작한다.
  - 필드 누락 가능성 감소
  - 효율적인 SQL 생성
  - 데이터베이스 통신 최소화
  - 캐시 이점 활용

  <strong> 문제 해결</strong>
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
  JPA가 권장하는 변경감지로 동작하도록 명시적으로 save()를 사용하지 않고, 엔티티를 그대로 반환했다. 이제 엔티티는 캐시에 저장된 수정 전의 엔티티와 비교하여 변경감지의 대상이 되어 수정된 필드의 값만 변경한
  적절한 update 쿼리를 날려준다.
  

**결과 쿼리**

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
    
</details>

### 회고/피드백
**만족한점** </br>
<details>
  <summary>1. APP 개발 과정 중 발생한 트러블 슈팅 과제들을 해결하여 최종적으로 동작하는 서비스를 개발했다.</summary>
  <p>
    </br>
    인프런 강의 내용이나 검색등을 통해 위의 트러블 슈팅들을 해결하며 최종적으로 쇼핑몰 서비스를 제공하는 웹 사이트를 개발함으로써 뿌듯함을 느꼈습니다.
  </p>
</details>
<details>
  <summary>2. front 분들과 소통하며 Back과 API 통신을 기반으로 동작하는 SPA 를 개발하고, 이 과정에서 프론트 지식을 배울 수 있었다.</summary>
  <p style="padding-left: 20px;">
    </br>
    프론트엔드 쪽과 통신하면서 서로 간 알고 있는 부분이 달라 대화가 잘 되지 않는 것을 발견 할수 있었습니다. 
    예를 들어 어떤 형식으로 보내줘야만 프론트쪽에서 편하게 데이터를 이용할 수 있는지, 네트워크 통신과정에서 에러가 발생했는데 누구의 에러인지 모르는 등과 같은 이슈가 발생했었습니다. </br>
  </br>
  이 과정들을 해결하기 위해 점심시간마다 정기적인 회의를 가지고, 각자의 문제를 공유하거나 기능들이 어떻게 동작해주는지 설명해주는 시간을 가졌습니다.
  이를 통해 Json 을 보내주면 프론트 쪽에서 객체로 바꾸어 페이지의 여러곳에서 사용한다는 점이나 웹과 WAS의 전체적인 아키텍처가 어떻게 동작하는지를 이해할 수 있었습니다.</br>
  </p>
</details>

**아쉬운점** </br>
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


