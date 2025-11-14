package dev.devrunner.api.techblog.dto;

import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.techblog.TechBlog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class TechBlogRead {
    private final Long techBlogId;
    private final String url;
    private final String company;
    private final String title;
    private final String oneLiner;
  //  private final String summary;
    private final String markdownBody;
    private final String thumbnailUrl;
    private final List<TechCategory> techCategories;
    private final String originalUrl;
    private final Popularity popularity;
    private final Boolean isDeleted;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static TechBlogRead from(TechBlog techBlog) {
        return new TechBlogRead(
                techBlog.getTechBlogId(),
                techBlog.getUrl(),
                techBlog.getCompany(),
                techBlog.getTitle(),
                techBlog.getOneLiner(),
             //   techBlog.getSummary(),
                techBlog.getMarkdownBody(),
                techBlog.getThumbnailUrl(),
                techBlog.getTechCategories(),
                techBlog.getOriginalUrl(),
                techBlog.getPopularity(),
                techBlog.getIsDeleted(),
                techBlog.getCreatedAt(),
                techBlog.getUpdatedAt()
        );
    }
}
