package dev.devrunner.search.techblog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * TechBlog search request DTO
 */
@Getter
@NoArgsConstructor
@ToString
public class TechBlogSearchRequest {

    // Filter conditions
    private String company;
    private List<String> techCategories;

    // Text search (searches across title and summary)
    private String searchWord;

    // Pagination (nullable)
    private Integer from;
    private Integer to;

    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_TO = 1000;

    /**
     * Get normalized from value
     * - Returns 0 if null
     * - Returns 0 if negative
     */
    public int getFrom() {
        if (from == null) {
            return DEFAULT_FROM;
        }
        return Math.max(0, from);
    }

    /**
     * Get normalized to value
     * - Returns 0~30 if both from and to are null
     * - Returns from~(from+30) if only from is provided
     * - Returns min(to, 1000) if to is provided
     * - to must always be greater than from
     */
    public int getTo() {
        int normalizedFrom = getFrom();

        if (to == null) {
            // When from is null or only from is provided
            return normalizedFrom + DEFAULT_PAGE_SIZE;
        }

        // When to is provided: max 1000, must be greater than from
        int normalizedTo = Math.min(to, MAX_TO);
        return Math.max(normalizedFrom + 1, normalizedTo);
    }
}
