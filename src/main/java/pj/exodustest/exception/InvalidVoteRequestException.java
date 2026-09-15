package pj.exodustest.exception;

/**
 * choice 누락/오타, voterId 누락 등 요청 자체가 잘못된 경우. 400 Bad Request 로 변환된다.
 */
public class InvalidVoteRequestException extends RuntimeException {

    public InvalidVoteRequestException(String message) {
        super(message);
    }
}
