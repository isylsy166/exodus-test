-- 투표 테이블
-- 컨테이너 재시작 시에도 매번 실행되므로 IF NOT EXISTS 로 멱등성을 보장한다.
-- (MySQL 은 CREATE INDEX ... IF NOT EXISTS 를 지원하지 않으므로 인덱스도 테이블 정의 안에 둔다)
CREATE TABLE IF NOT EXISTS vote
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    voter_id   VARCHAR(100) NOT NULL,
    choice     VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    -- 중복 투표 방지의 최종 방어선. 동시 요청에서도 DB가 단일 진실 공급원 역할을 한다.
    CONSTRAINT uk_vote_voter_id UNIQUE (voter_id),
    -- GET /api/result 의 choice 별 COUNT 집계용
    KEY idx_vote_choice (choice),
    -- 잘못된 choice 값이 애플리케이션을 우회해 저장되는 것을 막는다.
    CONSTRAINT ck_vote_choice CHECK (choice IN ('JAJANG', 'JJAMPPONG'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
