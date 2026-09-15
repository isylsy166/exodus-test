package pj.exodustest.dto;

import pj.exodustest.domain.VoteChoice;

/**
 * choice 별 집계 결과 한 줄. JPQL 생성자 표현식으로 직접 매핑된다.
 */
public record VoteCount(VoteChoice choice, long count) {
}
