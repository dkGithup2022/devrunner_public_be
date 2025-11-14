package dev.devrunner.elasticsearch.mapper;

import dev.devrunner.elasticsearch.document.TechBlogDoc;
import dev.devrunner.elasticsearch.vector.E5Vectorizer;
import dev.devrunner.elasticsearch.vector.VectorizeException;
import dev.devrunner.model.techblog.TechBlog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static dev.devrunner.elasticsearch.util.InstantConverter.toEpochMillis;

/**
 * Mapper for converting TechBlog domain model to TechBlogDoc (Elasticsearch document)
 * Maps to flattened TechBlogDoc structure for optimal Elasticsearch indexing
 */
@Component
@RequiredArgsConstructor
public class TechBlogDocMapper {

    private final E5Vectorizer vectorizer;

    /**
     * Convert TechBlog domain model to TechBlogDoc (with vectorization)
     *
     * @param techBlog the techBlog domain model
     * @return TechBlogDoc for Elasticsearch indexing
     */
    public TechBlogDoc newDoc(TechBlog techBlog) {
        if (techBlog == null) {
            return null;
        }

        var popularity = techBlog.getPopularity();

        // Vector 생성 (summary 기반)
        var vector = techBlog.getSummary() != null
                ? vectorizer.vectorize(techBlog.getSummary())
                : null;

        if (vector == null) {
            throw new VectorizeException(
                    "Cannot index TechBlog without summary (required for vectorization) - techBlogId: "
                            + techBlog.getTechBlogId()
            );
        }

        // Tech categories (enum → String)
        var categories = techBlog.getTechCategories() != null
                ? techBlog.getTechCategories().stream()
                .map(Enum::name)
                .collect(Collectors.toList())
                : null;


        return TechBlogDoc.of(
                // Document metadata
                generateDocId(techBlog),
                techBlog.getTechBlogId(),
                techBlog.getUrl(),
                techBlog.getCompany(),
                techBlog.getTitle(),
                techBlog.getOneLiner(),
                techBlog.getSummary(),
                techBlog.getSummaryKo(),  // koreanSummary - TODO: 파이프라인에서 생성 후 전달
                techBlog.getMarkdownBody(),
                techBlog.getThumbnailUrl(),

                // Tech categories
                categories,

                techBlog.getOriginalUrl(),

                // Popularity fields (flattened from Popularity)
                popularity != null ? popularity.getViewCount() : null,
                popularity != null ? popularity.getCommentCount() : null,
                popularity != null ? popularity.getLikeCount() : null,

                // Meta fields
                techBlog.getIsDeleted(),
                toEpochMillis(techBlog.getCreatedAt()),
                toEpochMillis(techBlog.getUpdatedAt()),

                // Vector field (generated from summary)
                vector
        );
    }

    /**
     * Generate document ID for Elasticsearch
     * Format: "techblog_{techBlogId}"
     */
    private String generateDocId(TechBlog techBlog) {
        return "techblog_" + techBlog.getTechBlogId();
    }
}
