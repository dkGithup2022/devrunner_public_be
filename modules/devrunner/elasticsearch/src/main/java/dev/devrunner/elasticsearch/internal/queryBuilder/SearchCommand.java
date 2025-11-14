package dev.devrunner.elasticsearch.internal.queryBuilder;

import java.util.List;

/** 공통 검색 커맨드: Hiring / Job / Post 등 모든 인덱스에 사용 */
public record SearchCommand<F extends FieldName>(
        List<SearchElement<F>> conditions,
        int from,
        int to    // exclusive
) {
    public SearchCommand {
        conditions = (conditions== null) ? List.of() : List.copyOf(conditions);
        from = Math.max(0, from);
        if (to < from) throw new IllegalArgumentException("to must be >= from");
    }

    /** ES size 계산용 (size = to - from) */
    public int size() { return to - from; }

    public static <F extends FieldName> SearchCommand<F> of(List<SearchElement<F>> conditions, int from, int to) {
        return new SearchCommand<>(conditions, from, to);
    }
}

/*
// Job 인덱스
var reqs = List.of(
    new SearchElement<>(JobIndexField.TITLE, "주니어"),
    new SearchElement<>(JobIndexField.CAREER_LEVEL, CareerLevel.ENTRY),
    new SearchElement<>(JobIndexField.MIN_YEARS, 0, 2),
    new SearchElement<>(JobIndexField.STARTED_AT, LocalDateTime.now().minusDays(14), LocalDateTime.now())
);
SearchCommand<JobIndexField> cmd = SearchCommand.of(reqs, 0, 20);

// Post 인덱스
var postReqs = List.of(
    new SearchElement<>(PostIndexField.TITLE, "k8s"),
    new SearchElement<>(PostIndexField.CREATED_AT, LocalDateTime.now().minusDays(7), null)
);
SearchCommand<PostIndexField> postCmd = new SearchCommand<>(postReqs, 20, 40);
*
* */