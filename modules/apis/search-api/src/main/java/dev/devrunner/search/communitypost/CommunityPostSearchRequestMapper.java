package dev.devrunner.search.communitypost;

import dev.devrunner.elasticsearch.document.fieldSpec.communitypost.CommunityPostIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.communitypost.dto.CommunityPostSearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * CommunityPostSearchRequest를 SearchElement 리스트로 변환하는 매퍼
 */
public class CommunityPostSearchRequestMapper {

    /**
     * CommunityPostSearchRequest를 SearchElement 리스트로 변환 (searchWord 포함)
     */
    public static List<SearchElement<CommunityPostIndexField>> toElements(CommunityPostSearchRequest request) {
        List<SearchElement<CommunityPostIndexField>> elements = toElementsWithoutSearchWord(request);

        // searchWord - title OR markdownBody
        if (request.getSearchWord() != null && !request.getSearchWord().isBlank()) {
            elements.add(match(CommunityPostIndexField.TITLE, request.getSearchWord()));
            elements.add(match(CommunityPostIndexField.MARKDOWN_BODY, request.getSearchWord()));
        }

        return elements;
    }

    /**
     * CommunityPostSearchRequest를 SearchElement 리스트로 변환 (searchWord 제외)
     */
    public static List<SearchElement<CommunityPostIndexField>> toElementsWithoutSearchWord(CommunityPostSearchRequest request) {
        List<SearchElement<CommunityPostIndexField>> elements = new ArrayList<>();

        // 1. Category
        addIfPresent(request.getCategory(), v -> elements.add(term(CommunityPostIndexField.CATEGORY, v)));

        // 2. Company
        addIfPresent(request.getCompany(), v -> elements.add(term(CommunityPostIndexField.COMPANY, v)));

        // 3. Location
        addIfPresent(request.getLocation(), v -> elements.add(term(CommunityPostIndexField.LOCATION, v)));

        // 4. IsFromJobComment
        addIfPresent(request.getIsFromJobComment(),
                v -> elements.add(term(CommunityPostIndexField.IS_FROM_JOB_COMMENT, v.toString())));

        // 5. Deleted filter (default: false)
        elements.add(term(CommunityPostIndexField.DELETED, "false"));

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
    private static SearchElement<CommunityPostIndexField> term(CommunityPostIndexField field, String value) {
        return new SearchElement<>(field, value);
    }

    /**
     * Match query용 SearchElement 생성 (QueryType에 따라 내부 처리됨)
     */
    private static SearchElement<CommunityPostIndexField> match(CommunityPostIndexField field, String value) {
        return new SearchElement<>(field, value);
    }
}
