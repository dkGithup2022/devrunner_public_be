package dev.devrunner.jdbc.bookmark.repository;

import dev.devrunner.infra.bookmark.repository.BookmarkRepository;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bookmark Repository 구현체
 *
 * 헥사고날 아키텍처에서 Adapter 역할을 수행하며,
 * Infrastructure의 BookmarkRepository 인터페이스를
 * Spring Data JDBC를 활용하여 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class BookmarkJdbcRepository implements BookmarkRepository {

    private final BookmarkEntityRepository entityRepository;

    @Override
    public Optional<Bookmark> findById(BookmarkIdentity identity) {
        return entityRepository.findById(identity.getBookmarkId())
                .map(this::toDomain);
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        BookmarkEntity entity = toEntity(bookmark);
        BookmarkEntity saved = entityRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(BookmarkIdentity identity) {
        entityRepository.deleteById(identity.getBookmarkId());
    }

    @Override
    public boolean existsById(BookmarkIdentity identity) {
        return entityRepository.existsById(identity.getBookmarkId());
    }

    @Override
    public List<Bookmark> findByUserId(UserIdentity userIdentity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entityRepository.findByUserId(userIdentity.getUserId(), pageable).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Bookmark> findByUserIdAndTargetType(UserIdentity userIdentity, TargetType targetType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return entityRepository.findByUserIdAndTargetType(userIdentity.getUserId(), targetType, pageable).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(UserIdentity userIdentity) {
        return entityRepository.countByUserId(userIdentity.getUserId());
    }

    @Override
    public Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(UserIdentity userIdentity, TargetType targetType, Long targetId) {
        return entityRepository.findByUserIdAndTargetTypeAndTargetId(userIdentity.getUserId(), targetType, targetId)
                .map(this::toDomain);
    }

    /**
     * Entity ↔ Domain 변환 메서드
     */
    private Bookmark toDomain(BookmarkEntity entity) {
        return new Bookmark(
                entity.getId(),
                entity.getUserId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private BookmarkEntity toEntity(Bookmark domain) {
        return new BookmarkEntity(
                domain.getBookmarkId(),
                domain.getUserId(),
                domain.getTargetType(),
                domain.getTargetId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
