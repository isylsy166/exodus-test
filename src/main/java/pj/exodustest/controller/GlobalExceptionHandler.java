package pj.exodustest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pj.exodustest.dto.ErrorResponse;
import pj.exodustest.exception.DuplicateVoteException;
import pj.exodustest.exception.InvalidVoteRequestException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 이미 투표한 voterId. */
    @ExceptionHandler(DuplicateVoteException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateVote(DuplicateVoteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_VOTE", e.getMessage()));
    }

    /** choice 오타, voterId 누락 등. */
    @ExceptionHandler(InvalidVoteRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidVoteRequestException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));
    }

    /** 본문이 비었거나 JSON 이 깨진 경우. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_REQUEST", "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해 주세요."));
    }
}
