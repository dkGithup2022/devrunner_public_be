package dev.devrunner.elasticsearch.internal.utils;

import java.util.List;

public class PaginationUtils {

    public static <T> PaginatedResult<T> paginate(List<T> searchResults, int requestedSize) {
        boolean hasNext = searchResults.size() > requestedSize;
        List<T> resultData = hasNext ? searchResults.subList(0, requestedSize) : searchResults;
        return new PaginatedResult<>(resultData, hasNext);
    }

    public static PaginationInfo calculatePaginationInfo(Integer from, Integer to, int defaultPageSize) {
        int actualFrom = from != null ? from : 0;
        int actualTo = to != null ? to : actualFrom + defaultPageSize;
        int requestedSize = actualTo - actualFrom;
        int searchSize = requestedSize + 1; // hasNext 판단을 위해 +1

        return new PaginationInfo(actualFrom, actualTo, requestedSize, searchSize);
    }

    public record PaginationInfo(int from, int to, int requestedSize, int searchSize) {}
    public record PaginatedResult<T>(List<T> data, boolean hasNext) {}
}
