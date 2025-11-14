package dev.devrunner.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

/**
 * TechBlogDoc - Elasticsearch document for TechBlog search
 *
 * Flattened structure for optimal Elasticsearch indexing and querying.
 * All nested objects are flattened to simple fields with prefixes.
 */
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechBlogDoc implements DocBase {

    // ES Type: keyword (used as document ID)
    @JsonProperty("doc_id")
    String docId;

    // ES Type: long
    @JsonProperty("tech_blog_id")
    Long techBlogId;

    // ES Type: keyword
    @JsonProperty("url")
    String url;

    // ES Type: keyword
    @JsonProperty("company")
    String company;

    // ES Type: text + keyword (multi-field for full-text search and exact match)
    @JsonProperty("title")
    String title;

    // ES Type: text
    @JsonProperty("one_liner")
    String oneLiner;

    // ES Type: text
    @JsonProperty("summary")
    String summary;

    // ES Type: text
    @JsonProperty("korean_summary")
    String koreanSummary;

    // ES Type: text
    @JsonProperty("markdown_body")
    String markdownBody;

    // ES Type: keyword
    @JsonProperty("thumbnail_url")
    String thumbnailUrl;

    // ES Type: keyword (array)
    @JsonProperty("tech_categories")
    List<String> techCategories;

    // ES Type: keyword
    @JsonProperty("original_url")
    String originalUrl;

    // Popularity fields (flattened from Popularity)
    // ES Type: long
    @JsonProperty("popularity_view_count")
    Long popularityViewCount;

    // ES Type: long
    @JsonProperty("popularity_comment_count")
    Long popularityCommentCount;

    // ES Type: long
    @JsonProperty("popularity_like_count")
    Long popularityLikeCount;

    // Meta fields
    // ES Type: boolean
    @JsonProperty("deleted")
    Boolean deleted;

    // ES Type: date (epoch_millis)
    @JsonProperty("created_at")
    Long createdAt;

    // ES Type: date (epoch_millis)
    @JsonProperty("updated_at")
    Long updatedAt;

    // ES Type: dense_vector
    @JsonProperty("vector")
    List<Float> vector;

    // ES Type: text
    // 만들어지는 값 .
    @JsonProperty("search_word")
    String searchWord;

    /**
     * searchWord를 자동 생성하는 정적 팩토리 메서드
     */
    public static TechBlogDoc of(
            String docId,
            Long techBlogId,
            String url,
            String company,
            String title,
            String oneLiner,
            String summary,
            String koreanSummary,
            String markdownBody,
            String thumbnailUrl,
            List<String> techCategories,
            String originalUrl,
            Long popularityViewCount,
            Long popularityCommentCount,
            Long popularityLikeCount,
            Boolean deleted,
            Long createdAt,
            Long updatedAt,
            List<Float> vector
    ) {
        String searchWord = buildSearchWord(title, company, techCategories, oneLiner, koreanSummary, summary);

        return new TechBlogDoc(
            docId, techBlogId, url, company, title, oneLiner, summary, koreanSummary,
            markdownBody, thumbnailUrl, techCategories, originalUrl,
            popularityViewCount, popularityCommentCount, popularityLikeCount,
            deleted, createdAt, updatedAt, vector, searchWord
        );
    }

    /**
     * searchWord 생성 로직
     * title + "\n" + company + "\n" + categories + "\n" + one_liner + "\n" + korean_summary + "\n" + summary
     */
    private static String buildSearchWord(
            String title,
            String company,
            List<String> techCategories,
            String oneLiner,
            String koreanSummary,
            String summary
    ) {
        StringBuilder sb = new StringBuilder();

        if (title != null && !title.isBlank()) {
            sb.append(title).append("\n");
        }

        if (company != null && !company.isBlank()) {
            sb.append(company).append("\n");
        }

        if (techCategories != null && !techCategories.isEmpty()) {
            sb.append(String.join(" ", techCategories)).append("\n");
        }

        if (oneLiner != null && !oneLiner.isBlank()) {
            sb.append(oneLiner).append("\n");
        }

        if (koreanSummary != null && !koreanSummary.isBlank()) {
            sb.append(koreanSummary).append("\n");
        }

        if (summary != null && !summary.isBlank()) {
            sb.append(summary);
        }

        return sb.toString().trim();
    }
}
