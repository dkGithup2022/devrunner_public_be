package dev.devrunner.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * CommunityPostDoc - Elasticsearch document for CommunityPost search
 *
 * Flattened structure for optimal Elasticsearch indexing and querying.
 * All nested objects are flattened to simple fields with prefixes.
 */
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityPostDoc implements DocBase {

    // ES Type: keyword (used as document ID)
    @JsonProperty("doc_id")
    String docId;

    // ES Type: long
    @JsonProperty("community_post_id")
    Long communityPostId;

    // ES Type: long
    @JsonProperty("user_id")
    Long userId;

    // ES Type: keyword
    @JsonProperty("category")
    String category;

    // ES Type: text + keyword (multi-field for full-text search and exact match)
    @JsonProperty("title")
    String title;

    // ES Type: text
    @JsonProperty("markdown_body")
    String markdownBody;

    // ES Type: keyword
    @JsonProperty("company")
    String company;

    // ES Type: keyword
    @JsonProperty("location")
    String location;

    // ES Type: long
    @JsonProperty("linked_job_id")
    Long linkedJobId;

    // ES Type: boolean
    @JsonProperty("is_from_job_comment")
    Boolean isFromJobComment;

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
}
