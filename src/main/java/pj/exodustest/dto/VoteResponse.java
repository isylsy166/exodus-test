package pj.exodustest.dto;

/**
 * POST /api/vote 성공 응답.
 */
public record VoteResponse(String voterId, String choice) {
}
