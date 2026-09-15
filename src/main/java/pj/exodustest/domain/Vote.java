package pj.exodustest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 한 명의 투표 기록.
 * <p>
 * {@code voter_id} 유니크 제약이 중복 투표 방지의 최종 방어선이다.
 * 동시 요청으로 선(先)조회가 뚫리더라도 INSERT 시점에 DB 가 걸러낸다.
 */
@Entity
@Table(
        name = "vote",
        uniqueConstraints = @UniqueConstraint(name = "uk_vote_voter_id", columnNames = "voter_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voter_id", nullable = false, length = 100)
    private String voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "choice", nullable = false, length = 20)
    private VoteChoice choice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Vote(String voterId, VoteChoice choice, LocalDateTime createdAt) {
        this.voterId = voterId;
        this.choice = choice;
        this.createdAt = createdAt;
    }

    public static Vote of(String voterId, VoteChoice choice) {
        return new Vote(voterId, choice, LocalDateTime.now());
    }
}
