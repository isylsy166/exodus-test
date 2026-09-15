package pj.exodustest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GET /api/result 응답.
 * <pre>
 * {"jajang": 120, "jjamppong": 95, "total": 215}
 * </pre>
 */
public record VoteResultResponse(
        @JsonProperty("jajang") long jajang,
        @JsonProperty("jjamppong") long jjamppong,
        @JsonProperty("total") long total
) {
    public static VoteResultResponse of(long jajang, long jjamppong) {
        return new VoteResultResponse(jajang, jjamppong, jajang + jjamppong);
    }
}
