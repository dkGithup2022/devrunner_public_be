package dev.devrunner.api.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 북마크 목록 조회 응답 DTO (페이지네이션 포함)
 */
@Getter
@AllArgsConstructor
public class BookmarkListResponse {

    private List<BookmarkResponse> bookmarks;
    private long totalCount;
    private int page;
    private int size;
}
