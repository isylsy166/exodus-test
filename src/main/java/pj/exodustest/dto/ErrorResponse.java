package pj.exodustest.dto;

/**
 * 오류 응답 공통 포맷.
 */
public record ErrorResponse(String error, String message) {
}
