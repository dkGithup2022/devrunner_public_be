package dev.devrunner.elasticsearch.internal.indexer;


import co.elastic.clients.elasticsearch._types.Result;

/**
 * Index response type mapped from Elasticsearch index request response.
 *
 * Indicates whether the index request was successfully indexed, updated, deleted,
 * or if there was an issue (not_found, noop, unknown).
 */
public enum IndexResponseType {
    Created("created"),
    Updated("updated"),
    Deleted("deleted"),
    NotFound("not_found"),
    NoOp("noop"),
    Unknown("unknown");

    private final String value;

    IndexResponseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert Elasticsearch Result to IndexResponseType
     * Returns UNKNOWN if the result value is not recognized
     *
     * @param result Elasticsearch Result object
     * @return corresponding IndexResponseType, or UNKNOWN if not matched
     */
    public static IndexResponseType from(Result result) {
        if (result == null) {
            return Unknown;
        }

        String jsonValue = result.jsonValue();
        for (IndexResponseType type : values()) {
            if (type.value.equals(jsonValue)) {
                return type;
            }
        }

        // 매핑되지 않은 경우 UNKNOWN 반환
        return Unknown;
    }
}
