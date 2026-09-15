package pj.exodustest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pj.exodustest.domain.Vote;
import pj.exodustest.domain.VoteChoice;
import pj.exodustest.dto.VoteCount;
import pj.exodustest.dto.VoteRequest;
import pj.exodustest.dto.VoteResponse;
import pj.exodustest.dto.VoteResultResponse;
import pj.exodustest.exception.DuplicateVoteException;
import pj.exodustest.exception.InvalidVoteRequestException;
import pj.exodustest.repository.VoteRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoteService {

    private static final int MAX_VOTER_ID_LENGTH = 100;

    private final VoteRepository voteRepository;

    /**
     * 투표를 저장한다.
     * <p>
     * 중복 방지는 2단계로 동작한다.
     * <ol>
     *   <li>{@code existsByVoterId} 로 빠르게 걸러낸다 (일반적인 재요청 처리).</li>
     *   <li>거의 동시에 들어온 요청이 1번을 함께 통과하더라도,
     *       {@code uk_vote_voter_id} 유니크 제약이 둘 중 하나만 커밋되도록 보장한다.</li>
     * </ol>
     * 애플리케이션 락을 쓰지 않으므로 인스턴스를 여러 개로 늘려도 정합성이 유지된다.
     */
    @Transactional
    public VoteResponse vote(VoteRequest request) {
        String voterId = normalizeVoterId(request);
        VoteChoice choice = parseChoice(request);

        if (voteRepository.existsByVoterId(voterId)) {
            throw new DuplicateVoteException(voterId);
        }

        try {
            // flush 를 강제해 제약 위반을 커밋 시점이 아닌 이 자리에서 잡는다.
            voteRepository.saveAndFlush(Vote.of(voterId, choice));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateVoteException(voterId);
        }

        return new VoteResponse(voterId, choice.apiValue());
    }

    /**
     * 현재 집계를 조회한다. 저장된 행을 매번 COUNT 하므로 실제 데이터와 항상 일치한다.
     */
    @Transactional(readOnly = true)
    public VoteResultResponse result() {
        List<VoteCount> counts = voteRepository.countGroupByChoice();

        Map<VoteChoice, Long> byChoice = new EnumMap<>(VoteChoice.class);
        for (VoteCount count : counts) {
            byChoice.put(count.choice(), count.count());
        }

        return VoteResultResponse.of(
                byChoice.getOrDefault(VoteChoice.JAJANG, 0L),
                byChoice.getOrDefault(VoteChoice.JJAMPPONG, 0L)
        );
    }

    private String normalizeVoterId(VoteRequest request) {
        if (request == null || request.voterId() == null || request.voterId().isBlank()) {
            throw new InvalidVoteRequestException("voterId 는 필수입니다.");
        }
        String voterId = request.voterId().trim();
        if (voterId.length() > MAX_VOTER_ID_LENGTH) {
            throw new InvalidVoteRequestException(
                    "voterId 는 " + MAX_VOTER_ID_LENGTH + "자를 넘을 수 없습니다.");
        }
        return voterId;
    }

    private VoteChoice parseChoice(VoteRequest request) {
        if (request == null || request.choice() == null || request.choice().isBlank()) {
            throw new InvalidVoteRequestException("choice 는 필수입니다. (jajang | jjamppong)");
        }
        VoteChoice choice = VoteChoice.from(request.choice().trim());
        if (choice == null) {
            throw new InvalidVoteRequestException(
                    "choice 값이 올바르지 않습니다: " + request.choice() + " (jajang | jjamppong)");
        }
        return choice;
    }
}
