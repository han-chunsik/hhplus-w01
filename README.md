# 1주차 과제
## 주제: TDD로 개발하기
> 일정: 2024년 12월 14일 ~ 20일
---
>### 📌 TASK
>- [ ] 기본 구현
>  - [ ] 포인트 충전
>    - [X] 기능 구현
>  - [ ] 포인트 사용
>    - [ ] 기능 구현
>  - [ ] 포인트 조회
>    - [ ] 기능 구현
>  - [ ] 포인트 내역 조회
>    - [ ] 기능 구현
>- [ ] 단위 테스트 구현
>  - [ ] 포인트 충전
>  - [ ] 포인트 사용
>  - [ ] 포인트 조회
>  - [ ] 포인트 내역 조회
>- [ ] 기본 과제
>  - [ ] 포인트 충전, 사용 정책 추가
>    - [ ] 기능 구현
>  - [ ] 동시성 제어
>    - [ ] 동시성 제어 설계
>    - [ ] 동시성 제어 구현
>    - [ ] 동시성 제어 통합 테스트
>- [ ] 심화 과제
>  - [ ] 동시성 제어 방식에 대한 분석 및 보고서 작성

# 📌 개요
## 1️⃣ 과제 필수 사항
- Nest.js 의 경우 Typescript , Spring 의 경우 Kotlin / Java 중 하나로 작성합니다.
    - 프로젝트에 첨부된 설정 파일은 수정하지 않도록 합니다.
- 테스트 케이스의 작성 및 작성 이유를 주석으로 작성하도록 합니다.
- 프로젝트 내의 주석을 참고하여 필요한 기능을 작성해주세요.
- 분산 환경은 고려하지 않습니다.
- `/point` 패키지 (디렉토리) 내에 `PointService` 기본 기능 작성
- `/database` 패키지의 구현체는 수정하지 않고, 이를 활용해 기능을 구현
- 각 기능에 대한 단위 테스트 작성
> 총 4가지 기본 기능 (포인트 조회, 포인트 충전/사용 내역 조회, 충전, 사용) 을 구현합니다.

## 2️⃣ 과제 상세
`point` 패키지의 TODO 와 테스트코드를 작성해주세요.

**요구 사항**  
포인트 조회, 포인트 충전/사용 내역 조회, 충전, 사용
```
- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- GET `/point/{id}` : 포인트를 조회한다.
- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
```
- 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

## 3️⃣ Pass 조건
### ✅ 기본과제
- 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
- 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
- 동시성 제어에 대한 통합 테스트 작성
### ✅ 심화과제
- 동시성 제어 방식에 대한 분석 및 보고서 작성 ( **README.md** )

---
# 📌 작업 기록

## 1️⃣ 프로젝트 템플릿 분석 및 요건 정의
### ✅ 테이블
```
+--------------------+       +-----------------------------+
|   UserPointTable   |       |      PointHistoryTable      |
+--------------------+       +-----------------------------+
| PK  id             |<------| FK  userId                  |
|     point          |       | PK  id                      |
|     updateMillis   |       |     amount                  |
+--------------------+       |     type (TransactionType)  |
                             |     updateMillis            |
                             +-----------------------------+
```

### UserPointTable
|컬럼 명|타입|설명|
|---|---|---|
|id|long|사용자 식별 id|
|point|long|해당 사용자의 잔여 point|
|updateMillis|long|업데이트 일자(밀리초 단위, 타임스탬프)|

### PointHistoryTable
|컬럼 명|타입|설명|
|---|---|---|
|id|long|트랜잭션 식별 id|
|userId|long|사용자 식별 id|
|amount|long|포인트 변경 금액|
|type|TransactionType|트랜잭션 유형 (CHARGE - 충전, USE - 사용)|
|updateMillis|long|업데이트 일자(밀리초 단위, 타임스탬프)|

### ✅ 포인트 트랜잭션 종류
| Type   | 설명 |
|--------|----|
| CHARGE | 충전 |
|USE| 사용 |

### ✅ API 명세 및 기능 요구사항
### 포인트 조회
- **Endpoint**
  - GET /{id}
- **Path Parameter**
  - id: (long) 조회할 유저의 ID
- **Response**
  ```json
    {
      "id": 1,
      "point": 1000,
      "updateMillis": 1672531200000
    }
  ```
- **기능 요구사항**
  - id로 특정 user의 포인트 조회
  - user가 존재하지 않을 경우 Exception 반환
- **행동분석**
  - 요청: 유저 ID를 받아 해당 유저의 포인트 정보를 반환
  - 동작:
    1. id가 유효한 값인지 확인
    2. 유저가 존재하는지 확인
    3. 유저의 포인트 정보를 반환
- **단위 테스트 케이스**
  - 성공
    - 유저가 존재하는 경우 올바른 포인트 정보를 반환한다.(Service)
  - 실패
    - id가 주어지지 않으면 조회를 실패한다. - 검증: id가 없는 경우 Service 호출을 하지 않는다. (Controller)
    - 주어진 id가 유효한 값이 아니면 조회를 실패한다. - 검증: id 검증 validator을 통해 Exception 반환받을 경우 이후 로직이 실행되지 않으며, 조회 실패. (Service)
    - 유저가 존재하지 않으면 조회를 실패한다. - 검증: 해당 id를가진 User가 없는 경우 Exception을 반환하며, 조회 실패 (Service)

### 포인트 충전/사용 내역 조회
- **Endpoint**
    - GET /{id}/histories
- **Path Parameter**
    - id: (long) 조회할 유저의 ID
- **Response**
  ```json
    [
      { 
        "id": 1,
        "userId": 1,
        "amount": 100,
        "updateMillis": 1672531200000,
        "type": "CHARGE"
      },
      { 
        "id": 2,
        "userId": 1,
        "amount": 100,
        "updateMillis": 1672531200000,
        "type": "USE"
      }
    ]
  ```
- **기능 요구사항**
  - 특정 유저의 포인트 내역 조회(충전 및 사용 이력)
  - 내역이 없을 경우 빈 배열 반환
- **고려사항**
  - 데이터 정렬: 날짜를 기준으로 최신 순으로 출력
  - 페이징 처리: 템플릿 소스에서 제공하는 controller에는 페이징 관련 파라미터가 없으므로 구현 제외
  - 실패한 내역 저장: 템플릿 소스에서 제공하는 데이터베이스는 충전/사용 타입 외 실패 내역을 저장할 수 있는 타입이 없으므로 구현 제외
  - 유저는 존재하지만 충전/사용 내역이 없는 경우: 빈 배열 반환(관리자에 의해 사용자만 별도 추가할 수도 있으니까,, 템플릿 소스 변경도 굳이 필요 없으니까 구현)
- **행동 분석**
  - 요청: 유저 ID를 받아 해당 유저의 포인트 충전/사용 내역 리스트를 반환
  - 동작
    1. id가 유효한 값인지 확인
    2. 유저가 존재하는지 확인
    3. 유저의 내역이 있으면 반환, 없으면 빈 배열을 반환
- **테스트 케이스**
  - 성공
    - 유저가 존재하며 내역이 있으면 내역 리스트를 반환한다. (Service)
    - 유저가 존재하지만 내역이 없으면 빈 배열을 반환한다. (Service)
  - 실패
    - id가 주어지지 않으면 조회를 실패한다. - 검증: id가 없는 경우 Service 호출을 하지 않는다. (Controller)
    - 주어진 id가 유효한 값이 아니면 조회를 실패한다. - 검증: id 검증 validator을 통해 Exception 반환받을 경우 이후 로직을 실행하지 않으며, 조회 실패. (Service)
    - 유저가 존재하지 않으면 조회를 실패한다. - 검증: 해당 id를가진 User가 있는지 확인 (Service)

### 포인트 충전
- **Endpoint**
    - PATCH /{id}/charge
- **Path Parameter**
    - id: (long) 포인트 충전할 유저의 ID
    - amount: (long) 충전 할 포인트
- **Response**
  ```json
    {
        "id": 1,
        "point": 1500,
        "updateMillis": 1672531500000
    }
  ```
- **기능 요구사항**
  - 요청받은 amount만큼 유저의 포인트를 충전.
  - 충전 금액은 반드시 0보다 커야 함.
- **고려사항**
  - 충전 실패 시 롤백: 현재 DB 구현체는 롤백을 제공하지 않으므로 제외, 요건을 충족하지 않으면 데이터 업데이트 로직 수행하지 않도록 구현
  - 최소 충전 포인트 정책: 1원의 포인트도 소중하므로 0 이상이면 무조건 가능하도록
  - 최초 충전하는 사용자(userTable에 없는 사용자): 사용자 추가
  - (추가 범위)최대 잔고 이상 충전 불가: 10,000,000 포인트 이상 충전 불가
- **행동 분석**
  - 요청: 유저 ID와 충전 금액을 받아 포인트를 충전
  - 동작:
    1. 입력된 id와 amount가 유효한 값인지 확인(Validation)
    2. 유저가 존재할 경우 현재 유저의 포인트 조회, 유저가 존재하지 않을 경우 0 (Service)
       - 유저가 존재하지 않는 경우 user 등록을 해버리면, 이후 로직이 실패할 경우 충전 히스토리 업데이트 등에 대한 고려사항이 애매하다고 판단되어, 유저 존재하지 않을 경우 0으로 변수에 선언, 이 후 기존 유저 있을떄와 동일한 시점에 UserTable에 등록하기로 함
    3. 포인트 충전 시 최대 잔고를 넘는지 확인(추가 범위)(Service)
    4. 충전 금액을 포인트에 더한 뒤 UserPointTable 업데이트(Service)
    5. 충전 히스토리 PointHistory에 업데이트(Service)
    6. 업데이트된 포인트 정보를 반환(Service)
- **테스트 케이스**
  - 성공
    - 등록되어있던 유저의 충전일 경우 기존 포인트 + 요청 포인트가 저장되었는지 확인 (Service)
    - 기 등록되지 않은 유저의 최초 충전일 경우 요청 포인트 금액과 현재 유저의 포인트가 동일한지 확인 (Service)
  - 실패
    - id나 amount가 주어지지 않으면 충전을 실패한다. - 검증: id나 amount가 없는 경우 Service 호출을 하지 않는다. (Controller)
    - 주어진 id나 amount가 유효한 값이 아니면 충전을 실패한다. - 검증: id, amount 검증 validator을 통해 Exception 반환받을 경우, 충전 실패. (Service)
    - 충전 후 포인트가 최대 잔고를 넘으면 충전을 실패한다.(추가 범위) 검증: 기존 포인트 + 충전 포인트가 최대 잔고를 넘을 경우 Exception 반환 (Service)

### 포인트 사용
- **Endpoint**
    - PATCH /{id}/use
- **Path Parameter**
    - id: (long) 포인트 사용할 유저의 ID
    - amount: (long) 사용 할 포인트
- **Response**
  ```json
    {
        "id": 1,
        "point": 1500,
        "updateMillis": 1672531500000
    }
  ```
- **기능 요구사항**
    - 요청받은 amount만큼 유저의 포인트를 사용.
    - 사용 금액은 반드시 0보다 커야 함.
- **고려사항**
  - 사용 실패 시 롤백: 현재 DB 구현체는 롤백을 제공하지 않으므로 제외, 요건을 충족하지 않으면 데이터 업데이트 로직 수행하지 않도록 구현
  - 최소 사용 포인트 정책: 1원의 포인트도 소중하므로 0 이상이면 무조건 가능하도록
  - (추가 범위)잔여 잔고 이상 사용 불가
- **행동 분석**
    - 요청: 유저 ID와 사용 금액을 받아 포인트 사용
    - 동작:
      1. 입력된 id와 amount가 유효한 값인지 확인(Validation)
      2. 유저가 존재하는지 확인(Service)
      3. 유저의 현재 포인트 조회(Service)
      4. 포인트 사용 금액이 현재 잔고를 넘는지 확인(추가 범위)(Service)
      5. 포인트 차감 후 UserPointTable 업데이트(Service)
      6. 차감 히스토리 PointHistory에 업데이트(Service)
      7. 업데이트된 포인트 정보를 반환(Service)
- **테스트 케이스**
  - 성공
    - 유저가 존재하며 충분한 잔액이 있을 경우 포인트가 정상적으로 차감된다 (Service).
  - 실패
    - id나 amount가 주어지지 않으면 충전을 실패한다. - 검증: id나 amount가 없는 경우 Service 호출을 하지 않는다. (Controller)
    - 주어진 id나 amount가 유효한 값이 아니면 충전을 실패한다. - 검증: id, amount 검증 validator을 통해 Exception 반환받을 경우, 충전 실패. (Service)
    - 유저가 존재하지 않으면 사용을 실패한다. - 검증: 해당 id를 가진 User가 있는지 확인 (Service)
    - 사용금액이 잔고를 넘으면 사용을 실패한다.(추가 범위) 검증: 사용 포인트가 잔고를 넘으면 Exception 반환 (Service)

> **고려사항 요약**  
> - 유저 존재 확인: 포인트 조회, 포인트 이력조회, 포인트 사용  
> - id, amount 유효성: 음수, 숫자가 아닌 경우 유효하지 않음

## 2️⃣ 키워드
### ✅ Given - When - Then 패턴
- **Given**: 준비: 테스트를 위한 준비, 변수 및 Mock 객체 정의
- **When**: 실행: 실제 기능을 수행하는 테스트
- **Then**: 검증: 예상한 값, 실제 행동 검증

### ✅ @Mock, @InjectMocks
- **@Mock**
  - 목적: 테스트 대상 클래스의 외부 의존성(예: DAO, Service 등)을 가짜(Mock) 객체로 만듬
  - 사용 이유: 테스트 중 실제 구현체 대신 Mock 객체를 주입하여 테스트 범위를 좁히고 독립적으로 테스트하기 위함
  - 특징:
    - 직접적인 호출만 허용하며, 설정된 동작(when-thenReturn)만 수행
    - 의존성을 호출해도 실제 로직은 실행되지 않음
- **@InjecMocks**
  - 목적: 테스트 대상 클래스의 의존성(필드, 생성자, setter)을 @Mock으로 생성된 객체로 자동 주입 
  - 사용 이유: 테스트 대상 클래스의 실제 로직을 실행하면서도 의존성은 Mock 객체로 대체
  - 특징:
    - 테스트하려는 대상 클래스에 실제 로직을 실행
    - Mock 객체로 주입된 의존성은 설정된 동작만 수행
> **@Mock, @InjectMocks 차이**  
> Mock 객체는 직접 호출해서 동작을 정의해야 하고, 독립적으로 테스트됨  
> - 의존성과 상관없이 가짜 동작만 테스트하려는 경우 사용  
> _내가 원하는 로봇을 만들어놓고, "너는 이렇게 행동해!"라고 구체적으로 알려주는 느낌_  
>
> InjectMocks는 Mock 객체를 내부적으로 주입받아 실제 로직을 실행  
> - 테스트 대상 로직(Service)의 동작을 확인하면서도, 의존성(Repository)은 Mock으로 대체하여 제어 가능.  
> _@InjectMocks: 내가 테스트하려는 실제 로직(예: UserService)을 실행하지만, 그 로직이 의존하는 로봇들(Mock)은 내가 원하는 대로 움직이게 설정_  

### ✅ Mock 검증(verify)
| **verify 메서드**                       | **설명**                           | **예제**                                          |
|--------------------------------------|----------------------------------|-------------------------------------------------|
| `verify(mock)`                       | 지정된 Mock 객체의 메서드가 호출되었는지 검증      | `verify(mock).method();`                        |
| `verify(mock, times(n))`             | 특정 메서드가 `n`번 호출되었는지 검증           | `verify(mock, times(2)).method();`              |
| `verify(mock, never())`              | 특정 메서드가 한 번도 호출되지 않았는지 검증        | `verify(mock, never()).method();`               |
| `verify(mock, atLeast(n))`           | 특정 메서드가 최소 `n`번 호출되었는지 검증        | `verify(mock, atLeast(2)).method();`            |
| `verify(mock, atMost(n))`            | 특정 메서드가 최대 `n`번 호출되었는지 검증        | `verify(mock, atMost(3)).method();`             |
| `verifyNoMoreInteractions(mock)`     | Mock 객체에 대해 더 이상 호출된 메서드가 없는지 검증 | `verifyNoMoreInteractions(mock);`               |
| `verifyZeroInteractions(mock)`       | Mock 객체에 대해 어떤 메서드도 호출되지 않았는지 검증 | `verifyZeroInteractions(mock);`                 |
| `verify(mock, timeout(ms))`          | 지정된 시간이 지나기 전에 메서드가 호출되었는지 검증    | `verify(mock, timeout(500)).method();`          |
| `verify(mock, timeout(ms).times(n))` | 특정 시간 내에 메서드가 `n`번 호출되었는지 검증     | `verify(mock, timeout(500).times(2)).method();` |

### ✅ JUnit 단정(assert)
| **assert 메서드**                           | **설명**                                           | **예제**                                             |
|------------------------------------------|--------------------------------------------------|-----------------------------------------------------|
| `assertEquals(expected, actual)`         | 두 값이 동일한지 검사합니다. 동일하지 않으면 테스트 실패                 | `assertEquals(5, add(2, 3));`                      |
| `assertNotEquals(expected, actual)`      | 두 값이 동일하지 않은지 검사합니다. 동일하면 테스트 실패                 | `assertNotEquals(4, add(2, 3));`                   |
| `assertTrue(condition)`                  | 조건이 `true`인지 검사합니다. `false`이면 테스트 실패             | `assertTrue(isEven(4));`                           |
| `assertFalse(condition)`                 | 조건이 `false`인지 검사합니다. `true`이면 테스트 실패             | `assertFalse(isEven(5));`                          |
| `assertNull(object)`                     | 객체가 `null`인지 검사합니다. `null`이 아니면 테스트 실패           | `assertNull(findById(999));`                       |
| `assertNotNull(object)`                  | 객체가 `null`이 아닌지 검사합니다. `null`이면 테스트 실패           | `assertNotNull(findById(1));`                      |
| `assertSame(expected, actual)`           | 두 객체가 동일한 인스턴스인지 검사 (객체의 참조를 비교)                 | `assertSame(obj1, obj2);`                          |
| `assertNotSame(expected, actual)`        | 두 객체가 동일한 인스턴스가 아닌지 검사                           | `assertNotSame(obj1, obj3);`                       |
| `assertArrayEquals(expected, actual)`    | 두 배열이 동일한지 검사, 배열의 길이와 내용이 모두 같아야 함              | `assertArrayEquals(new int[]{1, 2}, new int[]{1, 2});` |
| `assertThrows(expectedType, executable)` | 특정 예외가 발생했는지 검사                                  | `assertThrows(IllegalArgumentException.class, () -> { throwException(); });` |
| `fail()`                                 | 테스트를 강제로 실패 처리, 주로 구현 예정이거나 예상 외의 경로를 탐지하기 위해 사용 | `fail("This test should fail");`                   |

> **verify, assert 차이**  
> assert(결과 검증)
> - 결과를 확인하는 상태 검증이 필요한 경우.
> - 특정 입력에 대한 출력 값이 예상된 값인지 확인하려고 할 때.
> 
> verify(행위 검증)
> - 특정 메서드나 기능이 의도대로 호출되었는지 확인하려는 경우.
> - 복잡한 의존성(예: 데이터베이스, 서비스 호출 등)이 있는 코드에서 행위 기반 테스트가 필요할 때.