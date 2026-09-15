package pj.exodustest.exception;

/**
 * 이미 투표한 voterId 가 다시 투표를 시도한 경우. 409 Conflict 로 변환된다.
 */
public class DuplicateVoteException extends RuntimeException {

    public DuplicateVoteException(String voterId) {
        super("이미 투표한 voterId 입니다: " + voterId);
    }
}
