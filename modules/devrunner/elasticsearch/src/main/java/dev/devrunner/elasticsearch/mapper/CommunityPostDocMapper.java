package dev.devrunner.elasticsearch.mapper;

import dev.devrunner.elasticsearch.document.CommunityPostDoc;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostRead;
import org.springframework.stereotype.Component;

import static dev.devrunner.elasticsearch.util.InstantConverter.toEpochMillis;

/**
 * Mapper for converting CommunityPost domain model to CommunityPostDoc (Elasticsearch document)
 * Maps to flattened CommunityPostDoc structure for optimal Elasticsearch indexing
 */
@Component
public class CommunityPostDocMapper {

    /**
     * Convert CommunityPost domain model to CommunityPostDoc
     *
     * @param post the communityPost domain model
     * @return CommunityPostDoc for Elasticsearch indexing
     */
    public CommunityPostDoc toDoc(CommunityPost post) {
        if (post == null) {
            return null;
        }

        var popularity = post.getPopularity();
        var linkedContent = post.getLinkedContent();

        return new CommunityPostDoc(
            // Document metadata
            generateDocId(post),
            post.getCommunityPostId(),
            post.getUserId(),
            post.getCategory() != null ? post.getCategory().name() : null,
            post.getTitle(),
            post.getMarkdownBody(),
            post.getCompany(),
            post.getLocation(),
            linkedContent != null && linkedContent.getJobId() != null
                ? linkedContent.getJobId().getJobId()
                : null,
            linkedContent != null ? linkedContent.getIsFromJobComment() : null,

            // Popularity fields (flattened from Popularity)
            popularity != null ? popularity.getViewCount() : null,
            popularity != null ? popularity.getCommentCount() : null,
            popularity != null ? popularity.getLikeCount() : null,

            // Meta fields
            post.getIsDeleted(),
            toEpochMillis(post.getCreatedAt()),
            toEpochMillis(post.getUpdatedAt())
        );
    }

    public CommunityPostDoc toDoc(CommunityPostRead post) {
        if (post == null) {
            return null;
        }

        var popularity = post.getPopularity();
        var linkedContent = post.getLinkedContent();

        return new CommunityPostDoc(
                // Document metadata
                generateDocId(post),
                post.getCommunityPostId(),
                post.getUserId(),
                post.getCategory() != null ? post.getCategory().name() : null,
                post.getTitle(),
                post.getMarkdownBody(),
                post.getCompany(),
                post.getLocation(),
                linkedContent != null && linkedContent.getJobId() != null
                        ? linkedContent.getJobId().getJobId()
                        : null,
                linkedContent != null ? linkedContent.getIsFromJobComment() : null,

                // Popularity fields (flattened from Popularity)
                popularity != null ? popularity.getViewCount() : null,
                popularity != null ? popularity.getCommentCount() : null,
                popularity != null ? popularity.getLikeCount() : null,

                // Meta fields
                post.getIsDeleted(),
                toEpochMillis(post.getCreatedAt()),
                toEpochMillis(post.getUpdatedAt())
        );
    }

    /**
     * Generate document ID for Elasticsearch
     * Format: "communitypost_{communityPostId}"
     */
    private String generateDocId(CommunityPost post) {
        return "communitypost_" + post.getCommunityPostId();
    }

    private String generateDocId(CommunityPostRead post) {
        return "communitypost_" + post.getCommunityPostId();
    }
}
