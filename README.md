# 주유소 가격 순위 API (FuelRadar)

**CQRS 패턴을 적용한 주유소 가격 순위 조회 시스템**

## 설계 목표
주유소 가격 순위 데이터에서 **조회 성능 최적화**와 **최종적 일관성**을 동시에 해결

### 해결 전략
- **조회 성능 최적화**: Redis Sorted Set으로 순위 조회, Query Side 전용 캐시
- **최종적 일관성**: Event Sourcing으로 Write/Read 간 eventual consistency 보장
- **확장성**: CQRS로 Write/Read 독립 확장, 이벤트 기반 느슨한 결합

## CQRS 아키텍처

```
┌──────────────────────────┐      ┌─────────────────────────────┐      ┌────────────────────────┐
│ (Command Side)           │      │ (Event Broker & Projection) │      │ (Query Side)           │
│                          │      │                             │      │                        │
│ H2 Database              │ ───▶ │ Redis Stream                │ ───▶ │ Redis Sorted Set       │
│ GasStationCommandService │      │ GasStationEventListener     │      │ Redis Hash             │
│ GasStationEventPublisher │      │ GasStationProjectionService │      │ GasStationQueryService │
└──────────────────────────┘      └─────────────────────────────┘      └────────────────────────┘
```

### Command Side (Write)
- **H2 Database**: 주유소 원천 데이터 (CSV 초기 로딩)
- **GasStationCommandService**: 데이터 변경 + 이벤트 발행

### Event Broker
- **Redis Stream**: 이벤트 로그 (`gasstation:events`)
- **GasStationEventListener**: 이벤트 구독 및 Projection 업데이트
- **설계 참고**: 상용 환경에서는 Kafka가 일반적이나, 기존 Redis 의존성을 활용하여 Redis Stream으로 대체 구현

### Projection
- **GasStationProjectionService**: 이벤트 기반 Read Model 갱신
- **Redis Hash**: 주유소 상세 정보 캐싱 (`gasstation:detail:{code}`)
- **Redis Sorted Set**: 태그별 순위 저장

### Query Side (Read)
- **GasStationQueryService**: Redis Read Model 기반 순위 조회
- **GasStationQueryRepository**: Redis 전용 읽기 (RDB 참조 없음)

## 주요 기능

### Query API (가격 순위 조회)
```http
GET /api/stations/rankings/{tag}?page=1&size=50
```
- 가격 낮은 순: `gasoline` (휘발유), `diesel` (경유)
- 가격 상승폭 낮은 순: `gasoline-increase` (휘발유), `diesel-increase` (경유)
- Redis Sorted Set 기반 고성능 조회

### Command API
```http
POST /api/stations          # 주유소 생성
PUT /api/stations/{code}    # 주유소 정보 수정
```
- 가격 업데이트 시 기존 가격을 전일 가격으로 자동 적용

## 데이터 플로우

1. **데이터 변경** → H2 DB 업데이트 + Redis Stream 이벤트 발행
2. **이벤트 처리 & Projection** → Redis Stream 구독 + Redis Hash/Sorted Set 갱신
3. **순위 조회** → Redis Read Model에서 즉시 응답

## 기술 스택

- **Spring Boot + WebFlux + Kotlin**
- **Redis**: Sorted Set (순위) + Hash (주유소 정보 캐시) + Stream (이벤트)
- **H2 + R2DBC**
- **Testcontainers**: Redis Container 실행 목적

## 실행 방법

### 사전 조건
- Docker 설치 필요 (Redis Container 실행용)

### 실행
```bash
./gradlew bootRun    # 애플리케이션 실행
```

- **API 문서**: http://localhost:8080/swagger-ui.html
- **H2 콘솔**: http://localhost:8082
