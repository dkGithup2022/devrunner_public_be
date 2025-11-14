package dev.devrunner.elasticsearch.util;

import java.time.Instant;

/**
 * Instant와 epoch_millis(Long) 간 변환 유틸리티
 *
 * Elasticsearch date 타입은 epoch_millis를 받으므로,
 * Java Instant를 Long으로 변환하여 저장합니다.
 */
public class InstantConverter {

    /**
     * Instant를 epoch_millis(Long)로 변환
     * @param instant 변환할 Instant (null 가능)
     * @return epoch_millis 값, instant가 null이면 null 반환
     */
    public static Long toEpochMillis(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    /**
     * epoch_millis(Long)를 Instant로 변환
     * @param epochMillis epoch_millis 값 (null 가능)
     * @return Instant, epochMillis가 null이면 null 반환
     */
    public static Instant fromEpochMillis(Long epochMillis) {
        return epochMillis != null ? Instant.ofEpochMilli(epochMillis) : null;
    }
}
