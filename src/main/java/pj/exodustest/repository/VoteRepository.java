package pj.exodustest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pj.exodustest.domain.Vote;
import pj.exodustest.dto.VoteCount;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    /**
     * 중복 투표 빠른 실패용 선(先)조회.
     * <p>
     * 동시 요청에서는 이 검사와 INSERT 사이에 경쟁이 발생할 수 있으므로,
     * 최종 판정은 {@code uk_vote_voter_id} 유니크 제약이 담당한다.
     */
    boolean existsByVoterId(String voterId);

    /**
     * choice 별 득표수를 한 번의 쿼리로 집계한다.
     * <p>
     * 별도 카운터 컬럼을 두고 증가시키는 방식과 달리 lost update 자체가 발생하지 않으므로,
     * 집계 결과는 항상 실제 저장된 투표 수와 일치한다.
     */
    @Query("select new pj.exodustest.dto.VoteCount(v.choice, count(v)) from Vote v group by v.choice")
    List<VoteCount> countGroupByChoice();
}
