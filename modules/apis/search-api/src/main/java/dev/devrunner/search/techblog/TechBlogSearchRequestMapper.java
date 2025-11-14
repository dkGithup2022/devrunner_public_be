package dev.devrunner.search.techblog;

import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.techblog.dto.TechBlogSearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TechBlogSearchRequest를 SearchElement 리스트로 변환하는 매퍼
 */
public class TechBlogSearchRequestMapper {

    /**
     * TechBlogSearchRequest를 SearchElement 리스트로 변환 (searchWord 포함)
     */
    public static List<SearchElement<TechBlogIndexField>> toElements(TechBlogSearchRequest request) {
        List<SearchElement<TechBlogIndexField>> elements = toElementsWithoutSearchWord(request);

        // searchWord - SEARCH_WORD 단일 필드 검색
        if (request.getSearchWord() != null && !request.getSearchWord().isBlank()) {
            // title 은 두번 bm25 계산 .
            elements.add(match(TechBlogIndexField.TITLE, request.getSearchWord()));
            elements.add(match(TechBlogIndexField.SEARCH_WORD, request.getSearchWord()));
        }

        return elements;
    }

    /**
     * TechBlogSearchRequest를 SearchElement 리스트로 변환 (searchWord 제외)
     */
    public static List<SearchElement<TechBlogIndexField>> toElementsWithoutSearchWord(TechBlogSearchRequest request) {
        List<SearchElement<TechBlogIndexField>> elements = new ArrayList<>();

        // 1. Company
        addIfPresent(request.getCompany(), v -> elements.add(term(TechBlogIndexField.COMPANY, v)));

        // 2. Tech Categories (복수)
        if (request.getTechCategories() != null) {
            request.getTechCategories().forEach(tech ->
                    elements.add(term(TechBlogIndexField.TECH_CATEGORIES, tech)));
        }

        // 3. Deleted filter (default: false)
        elements.add(term(TechBlogIndexField.DELETED, "false"));

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
    private static SearchElement<TechBlogIndexField> term(TechBlogIndexField field, String value) {
        return new SearchElement<>(field, value);
    }

    /**
     * Match query용 SearchElement 생성 (QueryType에 따라 내부 처리됨)
     */
    private static SearchElement<TechBlogIndexField> match(TechBlogIndexField field, String value) {
        return new SearchElement<>(field, value);
    }
}
