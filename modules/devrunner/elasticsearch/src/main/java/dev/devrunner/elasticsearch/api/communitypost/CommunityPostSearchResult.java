package dev.devrunner.elasticsearch.api.communitypost;

import dev.devrunner.elasticsearch.document.CommunityPostDoc;

import java.util.List;

public record CommunityPostSearchResult(
        List<CommunityPostDoc> docs,
        boolean hasNext,
        long totalHits
) {
}
