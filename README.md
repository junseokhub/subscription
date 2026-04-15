# 구독 서비스 백엔드 API

## 기술 스택 

### Language & Framework
- Java21, Spring Boot 4.0.5 , JPA

### Database
- MySQL

### AI
- Spring AI 2.0.0-M4 (OpenAI)
- 구독 이력 조회의 summary 필드는 해당 라이브러리를 이용해 이력 데이터를 자연어로 요약하여 반환합니다.

### 외부 API 클라이언트
- RestClient 
- csrng 는 응답값을 받아야 트랜잭션 처리가 가능한 동기 구조라 WebClient + block() 보다 적합하고 판단했습니다.

### 빌드
- Gradle Kotlin DSL

---
## 폴더 구조
```
/channel
  - /domain
    - ChannelType
    - Channel
  - /repository
    - ChannelRepository
 
/common
  - /config
    - CsrngClient (외부 Csrng API 연동, 재시도 처리)
    - JpaConfig
    - OpenAiConfig (SpringAI OpenAI)
  - /entity
    - BaseEntity
  - /exception
    - BusinessException
    - ErrorCode
    - ErrorResponse
    - GlobalExceptionHandler
  - /properties
    - CsrngProperties
  - /response
    - ApiResponse

/member
  - /domain
    - Member
  - /repository
    - MemberRepository
  
/subscription
  - /domain
    - ActionType
    - Subscription
    - SubscriptionHistory
    - SubscriptionStatus
  - /repository
    - SubscriptionRepository
    - SubscriptionHistoryRepository
  - /service
    - SubscriptionService
    - SubscriptionHistoryService
  - /dto
    - HistoryResponse
    - SubscriptionRequest
    - SubscriptionResponse
  - /controller 
    - SubscriptionController
  
/resource
  - .env     # 환경 변수
  - data.sql # 실행 시 채널 등록
```
---

## 도메인 설계

### 엔티티 관계

```
Member (1) -> Subscription (1) -> SubscriptionHistory (N) <- Channel
```

### 구독 상태 (SubscriptionStatus)
- NONE(구독 안함) 
- NORMAL(일반 구독) 
- PREMIUM(프리미엄 구독)

### 구독 상태 변경 규칙
- 구독하기
  - 최초 가입 -> NONE, NORMAL, PREMIUM 모두 가능
  - NONE -> NORMAL, PREMIUM
  - NORMAL -> PREMIUM
  - PREMIUM -> 변경 불가

- 구독해지하기
  - PREMIUM -> NORMAL, NONE
  - NORMAL -> NONE
  - NONE -> 변경 불가

### 채널 타입 (ChannelType)
- 구독/해지 모두 가능 (BOTH)
- 구독만 가능 (SUBSCRIBE)
- 해지만 가능 (UNSUBSCRIBE)

### 이력 행위 타입 (ActionType)
- SUBSCRIBE(처음 구독)
- UPGRADE(구독을 업그레이드 하였습니다.)
- UNSUBSCRIBE(구독을 해지하였습니다.)

## API 명세
### 1. 구독하기

```
POST /api/subscriptions
```

- Request
```json
{
  "phoneNumber": "01012345678",
  "channelId": 1,
  "status": "NORMAL"
}
```

- Response(성공)
```json
{
  "success": true,
  "data": {
    "phoneNumber": "01012345678",
    "status": "NORMAL"
  },
  "error": null
}
```

- Response(실패)
```json
{
  "success": false,
  "data": null,
  "error": {
    "status": 500,
    "message": "외부 API 응답에 의해 트랜잭션이 롤백되었습니다.",
    "timestamp": "2026-04-13T00:00:00"
  }
}
```

### 2. 구독 해지

```
DELETE /api/subscriptions
```

- Request
```json
{
  "phoneNumber": "01012345678",
  "channelId": 1,
  "status": "NONE"
}
```

- Response(성공)
```json
{
  "success": true,
  "data": {
    "phoneNumber": "01012345678",
    "status": "NONE"
  },
  "error": null
}
```

### 3. 구독 이력 조회
- Request
```
GET /api/subscriptions/history?phoneNumber=01012345678
```

- Response(성공)
```json
{
  "success": true,
  "data": {
    "history": [
      {
        "channelName": "콜센터",
        "actionType": "UNSUBSCRIBE",
        "changedStatus": "NONE",
        "createdAt": "2026-04-13T00:00:00"
      },
      {
        "channelName": "홈페이지",
        "actionType": "UPGRADE",
        "changedStatus": "PREMIUM",
        "createdAt": "2026-04-13T00:00:00"
      },
      {
        "channelName": "홈페이지",
        "actionType": "SUBSCRIBE",
        "changedStatus": "NORMAL",
        "createdAt": "2026-04-13T00:00:00"
      }
    ],
    "summary": "2026년 4월 13일 홈페이지를 통해 일반 구독으로 가입한 뒤, 프리미엄으로 업그레이드하였습니다. 이후 콜센터를 통해 구독을 해지하여 현재 구독 안함 상태입니다."
  },
  "error": null
}
```
--
### 

## 프로젝트 실행 방법
### 사전 요구사항
- Java 21
- MySQL 8.x
- OpenAI API Key

### 환경변수 설정
- EnvFile Plugin 
- Run/Debug Configurations -> Enable EnvFile -> + 버튼 -> .env 지정

```bash
# .env example
SPRING_PROFILE=prod
DB_HOST=localhost
DB_PORT=3306
DB_NAME=subscription_db
DB_USERNAME=root
DB_PASSWORD=password
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o-mini
```
