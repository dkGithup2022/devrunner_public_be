package dev.devrunner.jdbc.communitypost.repository;

import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;
import dev.devrunner.model.communitypost.LinkedContent;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * CommunityPost Repository 구현체
 *
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 CommunityPostRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class CommunityPostJdbcRepository implements CommunityPostRepository {

    private final CommunityPostEntityRepository entityRepository;

    @Override
    public Optional<CommunityPostRead> findById(CommunityPostIdentity identity) {
        CommunityPostWithUserDto dto = entityRepository.findByIdWithUser(identity.getCommunityPostId());
        return Optional.ofNullable(dto).map(this::toCommunityPostRead);
    }

    @Override
    public CommunityPost save(CommunityPost communityPost) {
        CommunityPostEntity entity = toEntity(communityPost);
        CommunityPostEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(CommunityPostIdentity identity) {
        entityRepository.deleteById(identity.getCommunityPostId());
    }

    @Override
    public boolean existsById(CommunityPostIdentity identity) {
        return entityRepository.existsById(identity.getCommunityPostId());
    }

    @Override
    public List<CommunityPostRead> findAll() {
        return entityRepository.findAllWithUser().stream()
                .map(this::toCommunityPostRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunityPostRead> findByUserId(Long userId) {
        return entityRepository.findByUserIdWithUser(userId).stream()
                .map(this::toCommunityPostRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunityPostRead> findByCompany(String company) {
        return entityRepository.findByCompanyWithUser(company).stream()
                .map(this::toCommunityPostRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunityPostRead> findByLocation(String location) {
        return entityRepository.findByLocationWithUser(location).stream()
                .map(this::toCommunityPostRead)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunityPostRead> findByIds(List<CommunityPostIdentity> identities) {
        List<Long> ids = identities.stream()
                .map(CommunityPostIdentity::getCommunityPostId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return List.of();
        }

        return entityRepository.findByIdsWithUser(ids).stream()
                .map(this::toCommunityPostRead)
                .collect(Collectors.toList());
    }

    @Override
    public void increaseViewCount(CommunityPostIdentity identity, long increment) {
        entityRepository.increaseViewCount(identity.getCommunityPostId(), increment);
    }

    @Override
    public void increaseCommentCount(CommunityPostIdentity identity, long increment) {
        entityRepository.increaseCommentCount(identity.getCommunityPostId(), increment);
    }

    @Override
    public void increaseCommentCount(CommunityPostIdentity identity) {
        entityRepository.increaseCommentCount(identity.getCommunityPostId());
    }

    @Override
    public void increaseLikeCount(CommunityPostIdentity identity, long increment) {
        entityRepository.increaseLikeCount(identity.getCommunityPostId(), increment);
    }

    @Override
    public void increaseDislikeCount(CommunityPostIdentity identity, long increment) {
        entityRepository.increaseDislikeCount(identity.getCommunityPostId(), increment);
    }

    /**
     * DTO → CommunityPostRead 변환 (LEFT JOIN 결과 with nickname)
     */
    private CommunityPostRead toCommunityPostRead(CommunityPostWithUserDto dto) {
        Popularity popularity = new Popularity(
                dto.getViewCount(),
                dto.getCommentCount(),
                dto.getLikeCount(),
                dto.getDislikeCount()
        );

        LinkedContent linkedContent = createLinkedContent(dto.getJobId(), dto.getCommentId());

        return new CommunityPostRead(
                dto.getId(),
                dto.getUserId(),
                dto.getNickname(),
                dto.getCategory(),
                dto.getTitle(),
                dto.getMarkdownBody(),
                dto.getCompany(),
                dto.getLocation(),
                linkedContent,
                popularity,
                dto.getIsDeleted(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

    /**
     * Entity ↔ Domain 변환 메서드
     * Spring Data JDBC가 자동으로 embedded 객체를 처리
     */
    private CommunityPost toDomain(CommunityPostEntity entity) {
        LinkedContent linkedContent = createLinkedContent(entity.getJobId(), entity.getCommentId());

        return new CommunityPost(
                entity.getId(),
                entity.getUserId(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getMarkdownBody(),
                entity.getCompany(),
                entity.getLocation(),
                linkedContent,
                entity.getPopularity(),
                entity.getIsDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private LinkedContent createLinkedContent(Long jobId, Long commentId) {
        if (jobId == null && commentId == null) {
            return LinkedContent.none();
        }
        if (commentId != null) {
            return LinkedContent.fromJobComment(jobId, commentId);
        }
        return LinkedContent.fromJob(jobId);
    }

    private CommunityPostEntity toEntity(CommunityPost domain) {
        Long jobId = domain.getLinkedContent().getJobId() != null
                ? domain.getLinkedContent().getJobId().getJobId()
                : null;
        Long commentId = domain.getLinkedContent().getCommentId() != null
                ? domain.getLinkedContent().getCommentId().getCommentId()
                : null;

        return new CommunityPostEntity(
                domain.getCommunityPostId(),
                domain.getUserId(),
                domain.getCategory(),
                domain.getTitle(),
                domain.getMarkdownBody(),
                domain.getCompany(),
                domain.getLocation(),
                jobId,
                commentId,
                domain.getPopularity(),
                domain.getIsDeleted(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
