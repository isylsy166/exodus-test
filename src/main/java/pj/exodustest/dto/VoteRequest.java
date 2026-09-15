package pj.exodustest.dto;

/**
 * POST /api/vote 요청 본문.
 * <p>
 * 검증은 서비스 계층에서 수행하므로 여기서는 원본 문자열을 그대로 받는다.
 * (알 수 없는 choice 값을 enum 역직렬화 실패가 아니라 400 으로 명확히 돌려주기 위함)
 */
public record VoteRequest(String choice, String voterId) {
}
