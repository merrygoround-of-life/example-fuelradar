# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 & 실행
```bash
./gradlew bootRun         # 애플리케이션 실행 (Docker 필요 - Redis 컨테이너)
./gradlew test            # 테스트 실행
./gradlew compileKotlin   # 컴파일 확인
```

## 아키텍처
- **CQRS**: Command Side (H2) → Event Broker (Redis Stream) → Projection (Redis Hash/Sorted Set) → Query Side (Redis 읽기 전용)
- **함수형 라우팅**: RouterFunction 기반 (`config/RouterConfig.kt`), Swagger 문서화는 `@RouterOperations` 사용
- **Reactive**: WebFlux + Reactor, Lettuce auto-pipelining 활용

## 코드 컨벤션
- Reactor 병렬 처리: `Mono.zip` 사용 (Lettuce auto-pipelining으로 Redis round-trip 최소화)
- `Mono<Void>` 대신 가능한 의미 있는 반환 타입 사용

## Redis 키 구조
- `gasstation:events` — Redis Stream (이벤트 로그)
- `gasstation:detail:{stationCode}` — Hash (주유소 상세)
- `gasstation:rank:gasoline` — Sorted Set (휘발유 가격 낮은 순)
- `gasstation:rank:diesel` — Sorted Set (경유 가격 낮은 순)
- `gasstation:rank:gasoline-increase` — Sorted Set (휘발유 상승폭 낮은 순)
- `gasstation:rank:diesel-increase` — Sorted Set (경유 상승폭 낮은 순)

## API 엔드포인트
- `POST /api/stations` — 주유소 생성
- `PUT /api/stations/{stationCode}` — 주유소 수정
- `GET /api/stations/rankings/{tag}?page=1&size=50` — 순위 조회
- Swagger UI: http://localhost:8080/swagger-ui.html
