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

### 회고/피드백
**만족한점** </br>
<details>
  <summary>1. APP 개발 과정 중 발생한 트러블 슈팅 과제들을 해결하여 최종적으로 동작하는 서비스를 개발했다.</summary>
  <p>
    위에 작성한 트러블 슈팅들을 해결하며 최종적으로 쇼핑몰 서비스를 제공하는 웹 사이트를 개발해볼 수 있어서 좋았다.
  </p>
</details>
<details>
  <summary>2. 프론트 분들과 소통하며 API 통신을 기반으로 동작하는 SPA 를 개발하고, 이 과정에서 프론트 지식을 배울 수 있었다.</summary>
  <p>
    프론트엔드 쪽과 통신하면서 서로 간 알고 있는 부분이 달라 대화가 잘 되지 않는 것을 발견 할수 있었습니다. 예를 들어 어떤 형식으로 보내줘야만 프론트쪽에서 편하게 데이터를 이용할 수 있는지, 통신과정에서 에러가 발생했는데 누구의 에러인지 모르는 등과 같은 이슈가 발생했었습니다. </br>
    이 과정들을 해결하기 위해 점심시간마다 정기적인 회의를 가지고, 각자의 문제를 공유하거나 기능들이 어떻게 동작해주는지 설명해주는 시간을 가졌습니다.

    이를 통해 Json 을 보내주면 프론트 쪽에서 객체로 바꾸어 페이지의 여러곳에서 사용한다는 점이나 웹과 WAS의 전체적인 아키텍처가 어떻게 동작하는지를 이해할 수 있었습니다.
  </p>
</details>

**아쉬운점** </br>
<details>
  <summary> 1. 팀원들의 실력을 고려한 프로젝트를 기획 및 설계하지 못했다.</summary>
  <p>
    데이터베이스 설계나 네트워크, 디자인 패턴등 cs지식이 부족한 과정에서 기획과 설계를 진행하다보니 해답이 나오지 않는 부분에 시간을 너무 많이 허비했습니다. 팀원들 모두 완전한 지식을 가지고 있지 않았으므로, 일단은 아는 지식으로 개발 할 수 있는 정도의 APP을 개발 해두고, 하나하나 추가하거나 리팩토링 하는 방식으로 개발했어야 한다고 생각이 듭니다.
    
    예를 들어 ERD를 설계할 때 구매자, 판매자, 관리자를 어떻게 설계할 것인가로 이야기를 나누었는데, DB 설계 지식이 부족했다보니 무엇이 좋은 ERD 인가 고민하며 시간을 많이 허비했습니다. 간단하게 모두 도메인으로 만들어두고 기능을 개발하며 하나하나 필요하거나 새로운 기술들을 좀 더 적용해보면 개발 기간동안에 더 많은 것들을 할 수 있지 않았나 생각이 듭니다.
    
  </p>
</details>
<details>
  <summary> 2. 디테일한, 보기좋은 user or business sequence flow를 만들지 못한 부분</summary>
  <p>
    디테일하고 프론트, 백 양쪽이 이해할 수 있는 user sequence flow를 만들지 못하다보니, 이후 개발 과정에서 로직 중간과정은 어떻게 진행되는가를 물어보는 과정이 자주 일어났던것 같습니다. 차라리 처음에 flow를 그림으로 확고하게 그려놓고 부연설명을 붙여 놓은 후 조금조금씩 수정했다면 시간을 많이 절약할 수 있었을 것이라고 생각됩니다.
  </p>
</details>
</details>
<details>
  <summary> 3. 트러블 슈팅을 기록하고, 좀 더 깊은 고민을 해보지 않은 점</summary>
  <p>
    문제가 발생했다고 그냥 검색해서 해결하기 보다는 트러블 슈팅을 이슈 발견 - 원인 분석 - 해결방안(여러개) - 방안 후 결과 와 같이 단계적으로 깊게 고민하고 기록해두었으면 좋지 않았을까 생각됩니다. 문제가 발생했다고 웹에존재하는 해결방안으로 그냥 해결해버리면 해당 문제해결 과정을 깊게 이해하지도 못할 뿐만아니라 프로젝트가 끝난 후, 트러블 슈팅을 다시 정리하려니 기억이 디테일 하게 나지 않았습니다. (그리고 무엇보다 다시 문제를 찾고 과정을 작성하려면 귀찮았습니다.)
  </p>
</details>


