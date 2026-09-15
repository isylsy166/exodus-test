package pj.exodustest.domain;

/**
 * 투표 선택지.
 * <p>
 * API 요청/응답에서는 소문자({@code jajang}, {@code jjamppong})를 사용하고,
 * DB 에는 {@link Enum#name()} 기준의 대문자로 저장한다.
 */
public enum VoteChoice {

    JAJANG("jajang"),
    JJAMPPONG("jjamppong");

    private final String apiValue;

    VoteChoice(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    /**
     * API 로 들어온 문자열을 enum 으로 변환한다.
     *
     * @return 매칭되는 값이 없거나 null 이면 {@code null}
     */
    public static VoteChoice from(String value) {
        if (value == null) {
            return null;
        }
        for (VoteChoice choice : values()) {
            if (choice.apiValue.equalsIgnoreCase(value)) {
                return choice;
            }
        }
        return null;
    }
}
