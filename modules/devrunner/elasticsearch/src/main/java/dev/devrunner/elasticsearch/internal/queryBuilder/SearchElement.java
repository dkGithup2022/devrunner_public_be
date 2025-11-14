package dev.devrunner.elasticsearch.internal.queryBuilder;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
public class SearchElement<F extends FieldName> {

    private final F field;
    private final String value;  // TERM/MATCH 값
    private final String gte;    // RANGE 하한 (문자열로 통일)
    private final String lte;    // RANGE 상한 (문자열로 통일)

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    /** 풀 커스텀 생성자 */
    public SearchElement(F field, String value, String gte, String lte) {
        this.field = field;
        this.value = value;
        this.gte = gte;
        this.lte = lte;
    }

    /** TERM / MATCH */
    public SearchElement(F field, String value) {
        this(field, value, null, null);
    }
    public SearchElement(F field, boolean value) {
        this(field, String.valueOf(value), null, null);
    }
    public SearchElement(F field, Enum<?> value) {
        this(field, value == null ? null : value.name(), null, null);
    }
    public SearchElement(F field, Number value) {
        this(field, value == null ? null : String.valueOf(value), null, null);
    }

    /** RANGE (시간) — LocalDateTime → epochMillis(KST) */
    public SearchElement(F field, LocalDateTime gte, LocalDateTime lte) {
        this.field = field;
        this.value = null;
        this.gte = toEpochMillisString(gte);
        this.lte = toEpochMillisString(lte);
    }
    /** RANGE (시간) — Instant → epochMillis */
    public SearchElement(F field, Instant gte, Instant lte) {
        this(field,
                null,
                gte == null ? null : String.valueOf(gte.toEpochMilli()),
                lte == null ? null : String.valueOf(lte.toEpochMilli()));
    }

    /** RANGE (숫자) */
    public SearchElement(F field, Long gte, Long lte) {
        this(field,
                null,
                gte == null ? null : String.valueOf(gte),
                lte == null ? null : String.valueOf(lte));
    }
    public SearchElement(F field, Integer gte, Integer lte) {
        this(field,
                null,
                gte == null ? null : String.valueOf(gte),
                lte == null ? null : String.valueOf(lte));
    }
    public SearchElement(F field, Double gte, Double lte) {
        this(field,
                null,
                gte == null ? null : String.valueOf(gte),
                lte == null ? null : String.valueOf(lte));
    }

    private static String toEpochMillisString(LocalDateTime dt) {
        if (dt == null) return null;
        long epochMillis = dt.atZone(ZONE_ID).toInstant().toEpochMilli();
        return String.valueOf(epochMillis);
    }
}