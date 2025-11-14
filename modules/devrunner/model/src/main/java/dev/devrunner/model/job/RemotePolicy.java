package dev.devrunner.model.job;

/**
 * 재택 정책 Enum
 */
public enum RemotePolicy {
    REMOTE,     // 전면 원격, 100% 원격, 재택 근무, 풀리 리모트 등
    HYBRID,     // 주 몇 회 출근, 리모트 + 출근, 하이브리드, 오프라인 협업 병행 등
    ONSITE,     // 오피스 출근, 전일 출근, 지정된 사무실 근무 등
    UNKNOWN;    // 명확한 정책 정보가 없는 경우

    public static RemotePolicy fromString(String input) {
        try {
            return RemotePolicy.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid RemotePolicy: " + input);
        }
    }
}
