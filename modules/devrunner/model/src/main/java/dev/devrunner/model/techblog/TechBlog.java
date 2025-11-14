package dev.devrunner.model.techblog;

import dev.devrunner.model.AuditProps;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import lombok.Value;
import java.time.Instant;
import java.util.List;

@Value
public class TechBlog implements AuditProps {
    Long techBlogId;
    String url;
    String company;
    String title;
    String oneLiner;               // 한 줄 소개
    String summary;                // 요약본 (영어)
    String summaryKo;              // 요약본 (한국어)
    String markdownBody;
    String thumbnailUrl;
    List<TechCategory> techCategories;  // String → TechCategory enum
    String originalUrl;
    Popularity popularity;
    Boolean isDeleted;
    Instant createdAt;
    Instant updatedAt;

    public static TechBlog newBlog(
        String url,
        String title,
        String markdownBody
    ) {
        Instant now = Instant.now();
        return new TechBlog(
            null,
            url,
            null,
            title,
            null,  // oneLiner
            null,  // summary
            null,  // summaryKo
            markdownBody,
            null,
            List.of(),  // techCategories
            null,
            Popularity.empty(),
            false,
            now,
            now
        );
    }

    public static TechBlog newExternalBlog(
        String url,
        String company,
        String title,
        String markdownBody,
        String originalUrl
    ) {
        Instant now = Instant.now();
        return new TechBlog(
            null,
            url,
            company,
            title,
            null,  // oneLiner
            null,  // summary
            null,  // summaryKo
            markdownBody,
            null,
            List.of(),  // techCategories
            originalUrl,
            Popularity.empty(),
            false,
            now,
            now
        );
    }

    public TechBlog incrementViewCount() {
        return new TechBlog(
            techBlogId, url, company, title, oneLiner, summary, summaryKo, markdownBody, thumbnailUrl,
            techCategories, originalUrl, popularity.incrementViewCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public TechBlog incrementViewCount(long adder) {
        return new TechBlog(
            techBlogId, url, company, title, oneLiner, summary, summaryKo, markdownBody, thumbnailUrl,
            techCategories, originalUrl, popularity.incrementViewCount(adder),
            isDeleted, createdAt, Instant.now()
        );
    }

    public TechBlog incrementCommentCount() {
        return new TechBlog(
            techBlogId, url, company, title, oneLiner, summary, summaryKo, markdownBody, thumbnailUrl,
            techCategories, originalUrl, popularity.incrementCommentCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public TechBlog incrementLikeCount() {
        return new TechBlog(
            techBlogId, url, company, title, oneLiner, summary, summaryKo, markdownBody, thumbnailUrl,
            techCategories, originalUrl, popularity.incrementLikeCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public TechBlog decrementLikeCount() {
        return new TechBlog(
            techBlogId, url, company, title, oneLiner, summary, summaryKo, markdownBody, thumbnailUrl,
            techCategories, originalUrl, popularity.decrementLikeCount(),
            isDeleted, createdAt, Instant.now()
        );
    }

    public TechBlog markAsDeleted() {
        return new TechBlog(
            techBlogId, url, company, title, oneLiner, summary, summaryKo, markdownBody, thumbnailUrl,
            techCategories, originalUrl, popularity, true,
            createdAt, Instant.now()
        );
    }
}
