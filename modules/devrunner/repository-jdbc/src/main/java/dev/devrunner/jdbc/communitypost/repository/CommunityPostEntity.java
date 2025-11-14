package dev.devrunner.jdbc.communitypost.repository;

import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * CommunityPost Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 * - Popularity: @Embedded로 community_posts 테이블에 컬럼으로 저장
 */
@Table("community_posts")
@Getter
@AllArgsConstructor
public class CommunityPostEntity {
    @Id
    private Long id;
    private Long userId;
    private CommunityPostCategory category;
    private String title;
    private String markdownBody;
    private String company;
    private String location;
    private Long jobId;
    private Long commentId;

    // Embedded - community_posts 테이블에 컬럼으로 flatten
    @Embedded.Nullable
    private Popularity popularity;

    private Boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
}
