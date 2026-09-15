# 짜장면 vs 짬뽕 투표 API

짜장면과 짬뽕 중 하나에 투표하고 집계를 조회하는 백엔드 API입니다.

**Public URL** — https://game-spectator-canning.ngrok-free.dev

```bash
curl https://game-spectator-canning.ngrok-free.dev/health
curl https://game-spectator-canning.ngrok-free.dev/api/result
curl -X POST https://game-spectator-canning.ngrok-free.dev/api/vote \
  -H 'Content-Type: application/json' \
  -d '{"choice":"jajang","voterId":"user-123"}'
```

## API

| Method | Path | 설명 | 응답 |
|---|---|---|---|
| `POST` | `/api/vote` | 투표 | 200 성공 / 409 중복 / 400 잘못된 요청 |
| `GET` | `/api/result` | 집계 조회 | 200 |
| `GET` | `/health` | Health Check | 200 정상 / 503 DB 접속 불가 |

**POST /api/vote**

```json
// 요청
{"choice": "jajang", "voterId": "user-123"}   // choice: jajang | jjamppong

// 200
{"voterId": "user-123", "choice": "jajang"}

// 409 — 이미 투표한 voterId
{"error": "DUPLICATE_VOTE", "message": "이미 투표한 voterId 입니다: user-123"}

// 400 — choice 오타/누락, voterId 누락, 깨진 JSON
{"error": "INVALID_REQUEST", "message": "choice 값이 올바르지 않습니다: tangsuyuk (jajang | jjamppong)"}
```

**GET /api/result**

```json
{"jajang": 120, "jjamppong": 95, "total": 215}
```

**GET /health**

```json
{"status": "UP", "db": "UP", "time": "2026-09-15T15:02:05.354"}
```

## 실행 방법

### Docker (권장)

MySQL과 애플리케이션을 각각의 compose 파일로 띄웁니다. **앱 컨테이너가 MySQL이 만든 네트워크에 붙으므로 순서를 지켜야 합니다.**

```bash
# 1. MySQL 먼저 (네트워크 exodustest_default 생성)
docker compose -f docker-mysql.yml up -d

# 2. 애플리케이션
docker compose -f docker-spring.yml up -d --build
```

서비스는 **8080 포트**로 뜹니다.

```bash
curl http://localhost:8080/health
```

> **주의** — 코드를 수정한 뒤에는 반드시 `--build`를 붙여야 합니다. `docker compose up -d`만 실행하면 기존 이미지를 그대로 재사용해서 변경 사항이 반영되지 않습니다.

중지 / 재시작:

```bash
docker compose -f docker-spring.yml restart   # 앱만 재시작 (데이터 유지)
docker compose -f docker-spring.yml down      # 앱 중지
docker compose -f docker-mysql.yml down       # DB 중지 (볼륨은 유지)
```

### 로컬 실행

MySQL만 Docker로 띄우고 애플리케이션은 Gradle로 실행합니다.

```bash
docker compose -f docker-mysql.yml up -d
./gradlew bootRun
```

포트를 바꾸려면 `./gradlew bootRun --args='--server.port=8081'`.

## 사용한 기술

| 구분 | 선택 | 비고 |
|---|---|---|
| 언어 / 런타임 | Java 17 | Temurin |
| 프레임워크 | Spring Boot 4.1.1 | `spring-boot-starter-webmvc` |
| 데이터 접근 | Spring Data JPA (Hibernate 7) | |
| DB | MySQL 8.4 | Docker 컨테이너 |
| 빌드 | Gradle 9.7.1 (Wrapper) | |
| 기타 | Lombok | 보일러플레이트 제거 |
| 컨테이너 | Docker multi-stage build | JDK로 빌드 → JRE로 실행 |
| Public URL | ngrok | |

**구조** — 레이어 기반 패키지 구성입니다.

```
pj.exodustest
├── controller   VoteController, HealthController, GlobalExceptionHandler
├── service      VoteService
├── repository   VoteRepository
├── domain       Vote, VoteChoice
├── dto          VoteRequest/Response, VoteResultResponse, VoteCount, ErrorResponse
└── exception    DuplicateVoteException, InvalidVoteRequestException
```

## 데이터 저장 방식

MySQL 단일 테이블에 **투표 1건 = 행 1개**로 저장합니다.

```sql
CREATE TABLE IF NOT EXISTS vote
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    voter_id   VARCHAR(100) NOT NULL,
    choice     VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_vote_voter_id UNIQUE (voter_id),
    KEY idx_vote_choice (choice),
    CONSTRAINT ck_vote_choice CHECK (choice IN ('JAJANG', 'JJAMPPONG'))
) ENGINE = InnoDB;
```

- `uk_vote_voter_id` — 중복 투표 방지의 최종 방어선
- `idx_vote_choice` — `GET /api/result`의 `GROUP BY choice` 집계용
- `ck_vote_choice` — 애플리케이션을 우회한 잘못된 값 차단

**득표수를 저장하는 카운터 컬럼은 두지 않았습니다.** 집계는 조회 시점에 `COUNT`로 계산하므로 저장된 행과 항상 일치합니다.

스키마는 `src/main/resources/schema/schema.sql`에 두고 기동 시 실행합니다.

```yaml
spring:
  sql:
    init:
      mode: always                                  # 기본값 embedded는 MySQL에 적용되지 않음
      schema-locations: classpath:schema/schema.sql
  jpa:
    hibernate:
      ddl-auto: validate                            # 엔티티와 실제 테이블 불일치를 기동 시 검출
```

`CREATE TABLE IF NOT EXISTS`라 여러 번 실행해도 안전합니다. MySQL은 `CREATE INDEX ... IF NOT EXISTS`를 지원하지 않으므로 인덱스도 테이블 정의 안에 인라인으로 넣어 **재시작 시 스크립트가 실패하지 않도록** 했습니다.

스크립트 초기화는 JPA `EntityManagerFactory` 생성보다 먼저 수행되기 때문에, 테이블을 만든 직후 `ddl-auto: validate`가 검증하는 순서가 보장됩니다.

## 동시성 및 중복 투표 처리 방식

### 중복 투표 — 2단계 방어

```java
if (voteRepository.existsByVoterId(voterId)) {
    throw new DuplicateVoteException(voterId);   // 1단계: 일반적인 재요청 빠른 차단
}

try {
    voteRepository.saveAndFlush(Vote.of(voterId, choice));
} catch (DataIntegrityViolationException e) {
    throw new DuplicateVoteException(voterId);   // 2단계: 동시 요청 최종 차단
}
```

1단계 `existsByVoterId`는 **check-then-act라서 동시 요청에서는 뚫립니다.** 두 요청이 거의 동시에 들어오면 둘 다 "없음"을 보고 통과할 수 있습니다. 그래서 진짜 판정은 `uk_vote_voter_id` 유니크 제약이 담당하고, 위반 예외를 409로 변환합니다. 1단계는 성능을 위한 빠른 실패 경로일 뿐입니다.

`save`가 아니라 **`saveAndFlush`를 쓴 이유**는, flush를 강제하지 않으면 제약 위반이 트랜잭션 커밋 시점에 발생해 `try/catch` 밖으로 빠져나가 409가 아닌 500이 나가기 때문입니다.

### 집계 — 카운터 증가 대신 COUNT

```java
@Query("select new pj.exodustest.dto.VoteCount(v.choice, count(v)) from Vote v group by v.choice")
List<VoteCount> countGroupByChoice();
```

카운터 컬럼을 `UPDATE ... SET cnt = cnt + 1`로 올리는 방식은 lost update를 막기 위해 비관적 락이나 원자적 업데이트가 필요하고, 그만큼 경합 지점이 생깁니다. **행을 매번 세는 방식은 그 문제 자체가 발생하지 않습니다.** 집계 결과가 실제 성공한 투표 수와 어긋날 여지가 없습니다.

### 애플리케이션 락을 쓰지 않은 이유

`synchronized`나 JVM 내 락은 인스턴스가 하나일 때만 유효합니다. 정합성 판단을 **DB라는 단일 진실 공급원**에 위임하면 인스턴스를 수평 확장해도 동작이 그대로 유지됩니다.

### 검증 결과

| 시나리오 | 결과 |
|---|---|
| 동일 `voterId` 50건 동시 요청 | `1×200`, `49×409` — DB 실제 행 **1건** |
| 서로 다른 `voterId` 300건 동시 요청 (병렬 50) | `300×200` — 유실 0, API 집계 `{150,150,300}` = DB 실측 일치 |

## 재시작 후 데이터 유지 방식

투표 데이터는 프로세스 메모리가 아니라 **MySQL에 저장**되고, MySQL 데이터 디렉터리는 named volume에 보관됩니다.

```yaml
# docker-mysql.yml
volumes:
  - mysql-data:/var/lib/mysql
```

컨테이너를 재생성해도 볼륨은 남으므로 데이터가 유지됩니다. 애플리케이션 컨테이너는 상태를 갖지 않아서 언제든 자유롭게 교체할 수 있습니다.

두 컨테이너 모두 `restart: unless-stopped`라 Docker 데몬이나 호스트가 재시작돼도 자동으로 다시 올라옵니다.

중복 판정 기준도 메모리가 아니라 `vote` 테이블의 유니크 제약이므로, **재시작 후 동일 `voterId`가 다시 투표해도 기존 기록을 기준으로 409가 반환**됩니다.

**검증** — `docker restart exodustest-app` 실행 후:

- 집계 `{"jajang":2,"jjamppong":1,"total":3}` 그대로 유지
- 재시작 전에 투표했던 `voterId`로 재투표 → `409 DUPLICATE_VOTE`

## Public URL 구성 방식

ngrok 터널로 로컬 8080 포트를 외부에 노출했습니다.

```bash
ngrok http 8080
# → https://game-spectator-canning.ngrok-free.dev -> http://localhost:8080
```

클라우드 배포 대신 터널링을 선택한 이유는 **제한 시간 안에 외부 접속을 확실히 확보하기 위해서**입니다. 클라우드는 계정·네트워크·배포 파이프라인 설정에서 시간을 쓰게 되고 실패 지점도 많은 반면, 터널링은 즉시 동작하고 문제가 생겨도 원인이 명확합니다.

ngrok은 8080을 바라보고 컨테이너가 그 포트를 점유하므로, 애플리케이션을 재시작해도 터널 주소는 바뀌지 않습니다.

> **제약** — ngrok 프로세스가 종료되면 URL도 함께 내려갑니다. 운영 환경이라면 클라우드 배포나 고정 도메인이 적절하지만, 이번 과제 범위에서는 터널링으로 요구사항을 충족합니다.

## 구현하면서 중요하게 판단한 설계 사항

**1. 정합성 판단을 DB에 맡겼습니다.** 중복 방지는 유니크 제약, 집계는 COUNT 쿼리로 처리합니다. 애플리케이션 레벨에서 락을 잡거나 카운터를 관리하면 단일 인스턴스에서만 맞는 코드가 되고, 경합 조건을 직접 증명해야 합니다. DB 제약에 위임하면 동시성 정확성이 구조적으로 보장됩니다.

**2. Health Check가 DB까지 확인합니다.** 이 서비스는 투표를 DB에 저장하므로, DB에 붙지 못하는 상태에서 200을 돌려주면 health check가 거짓말을 하게 됩니다. `Connection.isValid(2)`로 확인해 정상이면 200, 실패하면 503을 반환합니다. 타임아웃을 2초로 짧게 잡은 것은 DB가 응답하지 않을 때 health 요청 자체가 매달리면 의미가 없기 때문입니다.

**3. `choice`를 enum이 아니라 String으로 받습니다.** DTO에서 enum으로 바로 역직렬화하면 오타가 있을 때 Jackson 역직렬화 예외가 발생해 원인을 알기 어려운 응답이 나갑니다. 원본 문자열로 받아 `VoteChoice.from()`으로 변환하고, 실패하면 어떤 값이 잘못됐는지 알려주는 400을 반환합니다. 별도의 validation 의존성도 필요하지 않습니다.

**4. 오류 응답 형식을 통일했습니다.** `@RestControllerAdvice`에서 `DuplicateVoteException → 409`, `InvalidVoteRequestException → 400`, `HttpMessageNotReadableException → 400`을 일관된 `{"error", "message"}` 포맷으로 변환합니다. 예외 처리가 컨트롤러에 흩어지지 않고, API를 호출하는 쪽에서 오류를 기계적으로 구분할 수 있습니다.

**5. 스키마를 SQL로 명시하고 `ddl-auto: validate`를 썼습니다.** `ddl-auto: update`는 편하지만 Hibernate가 무엇을 바꿀지 통제하기 어렵고, 유니크 제약이나 CHECK 제약처럼 정합성에 직결되는 요소를 코드로 남기지 못합니다. 스키마를 직접 작성하고 `validate`로 엔티티와의 불일치를 기동 시점에 검출하도록 했습니다.

**6. `open-in-view: false`.** 기본값(true)은 뷰 렌더링까지 영속성 컨텍스트와 DB 커넥션을 붙잡고 있어, 동시 요청이 많을 때 커넥션 풀이 고갈되기 쉽습니다. REST API에는 불필요합니다.

**7. Docker 이미지를 multi-stage로 구성했습니다.** JDK 이미지에서 빌드하고 JRE 이미지로 실행 결과만 옮겨 이미지 크기를 줄였고, `useradd`로 만든 non-root 사용자로 실행합니다.
