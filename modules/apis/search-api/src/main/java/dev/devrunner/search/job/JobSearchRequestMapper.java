package dev.devrunner.search.job;

import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.job.dto.JobSearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * JobSearchRequest를 SearchElement 리스트로 변환하는 매퍼
 */
public class JobSearchRequestMapper {

    /**
     * JobSearchRequest를 SearchElement 리스트로 변환 (searchWord 포함)
     */
    public static List<SearchElement<JobIndexField>> toElements(JobSearchRequest request) {
        List<SearchElement<JobIndexField>> elements = toElementsWithoutSearchWord(request);

        // searchWord - title OR oneLineSummary OR fullDescription
        if (request.getSearchWord() != null && !request.getSearchWord().isBlank()) {
            elements.add(match(JobIndexField.TITLE, request.getSearchWord()));
            elements.add(match(JobIndexField.ONE_LINE_SUMMARY, request.getSearchWord()));
            elements.add(match(JobIndexField.FULL_DESCRIPTION, request.getSearchWord()));
        }

        return elements;
    }

    /**
     * JobSearchRequest를 SearchElement 리스트로 변환 (searchWord 제외)
     */
    public static List<SearchElement<JobIndexField>> toElementsWithoutSearchWord(JobSearchRequest request) {
        List<SearchElement<JobIndexField>> elements = new ArrayList<>();

        // 1. Company
        addIfPresent(request.getCompany(), v -> elements.add(term(JobIndexField.COMPANY, v.name())));

        // 2. Locations (복수)
        if (request.getLocations() != null) {
            request.getLocations().forEach(loc -> elements.add(term(JobIndexField.LOCATIONS, loc)));
        }

        // 3. Employment Type
        addIfPresent(request.getEmploymentType(),
                v -> elements.add(term(JobIndexField.EMPLOYMENT_TYPE, v.name())));

        // 4. Career Level
        addIfPresent(request.getCareerLevel(),
                v -> elements.add(term(JobIndexField.CAREER_LEVEL, v.name())));

        // 5. Position Category
        addIfPresent(request.getPositionCategory(),
                v -> elements.add(term(JobIndexField.POSITION_CATEGORY, v.name())));

        // 6. Remote Policy
        addIfPresent(request.getRemotePolicy(),
                v -> elements.add(term(JobIndexField.REMOTE_POLICY, v.name())));

        // 7. Tech Categories (복수)
        if (request.getTechCategories() != null) {
            request.getTechCategories().forEach(tech ->
                    elements.add(term(JobIndexField.TECH_CATEGORIES, tech)));
        }

        // 8. Experience Years Range
        if (request.getMinYears() != null || request.getMaxYears() != null) {
            elements.add(range(JobIndexField.MIN_YEARS, request.getMinYears(), request.getMaxYears()));
        }

        // 9. Experience Required
        addIfPresent(request.getExperienceRequired(),
                v -> elements.add(term(JobIndexField.EXPERIENCE_REQUIRED, v.toString())));

        // 10. Job Status
        addIfPresent(request.getIsOpenEnded(),
                v -> elements.add(term(JobIndexField.IS_OPEN_ENDED, v.toString())));
        addIfPresent(request.getIsClosed(),
                v -> elements.add(term(JobIndexField.IS_CLOSED, v.toString())));

        // 11. Interview Process
        addIfPresent(request.getHasCodingTest(),
                v -> elements.add(term(JobIndexField.HAS_CODING_TEST, v.toString())));
        addIfPresent(request.getHasLiveCoding(),
                v -> elements.add(term(JobIndexField.HAS_LIVE_CODING, v.toString())));
        addIfPresent(request.getInterviewCountTo(),
                v -> elements.add(range(JobIndexField.INTERVIEW_COUNT, null, v)));
        addIfPresent(request.getInterviewDaysTo(),
                v -> elements.add(range(JobIndexField.INTERVIEW_DAYS, null, v)));

        // 12. Deleted filter (default: false)
        elements.add(term(JobIndexField.DELETED, "false"));

        return elements;
    }

    // === Helper Methods ===

    /**
     * null-safe helper: value가 null이 아닐 때만 consumer 실행
     */
    private static <T> void addIfPresent(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * Term query용 SearchElement 생성
     */
    private static SearchElement<JobIndexField> term(JobIndexField field, String value) {
        return new SearchElement<>(field, value);
    }

    /**
     * Range query용 SearchElement 생성
     */
    private static SearchElement<JobIndexField> range(JobIndexField field, Integer gte, Integer lte) {
        return new SearchElement<>(field, gte, lte);
    }

    /**
     * Match query용 SearchElement 생성 (QueryType에 따라 내부 처리됨)
     */
    private static SearchElement<JobIndexField> match(JobIndexField field, String value) {
        return new SearchElement<>(field, value);
    }
}
