package pj.exodustest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check.
 * <p>
 * 이 서비스는 투표 데이터를 DB 에 저장하므로, DB 에 연결하지 못하는 상태는 "정상"이 아니다.
 * 따라서 단순히 프로세스가 살아있는지만 보지 않고 커넥션 유효성까지 확인한 뒤,
 * 정상이면 200, DB 접속이 불가능하면 503 을 반환한다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    /** 커넥션 검증 타임아웃(초). DB 가 응답하지 않을 때 health 요청이 오래 매달리지 않도록 짧게 잡는다. */
    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbUp = isDatabaseUp();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", dbUp ? "UP" : "DOWN");
        body.put("db", dbUp ? "UP" : "DOWN");
        body.put("time", LocalDateTime.now());

        return dbUp
                ? ResponseEntity.ok(body)
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(VALIDATION_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.warn("health check: DB 연결 실패 - {}", e.getMessage());
            return false;
        }
    }
}
