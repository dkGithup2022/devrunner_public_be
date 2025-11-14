package dev.devrunner.service.bookmark.impl;

import dev.devrunner.infra.bookmark.repository.BookmarkRepository;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.exception.bookmark.BookmarkNotFoundException;
import dev.devrunner.service.bookmark.BookmarkReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Bookmark 도메인 조회 서비스 구현체
 *
 * CQRS 패턴의 Query 책임을 구현하며,
 * Infrastructure Repository를 활용한 조회 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultBookmarkReader implements BookmarkReader {

    private final BookmarkRepository bookmarkRepository;

    @Override
    public Bookmark getById(BookmarkIdentity identity) {
        log.debug("Fetching Bookmark by id: {}", identity.getBookmarkId());
        return bookmarkRepository.findById(identity)
                .orElseThrow(() -> new BookmarkNotFoundException("bookmark not found"));
    }

    @Override
    public List<Bookmark> getByUserId(UserIdentity userIdentity, int page, int size) {
        log.debug("Fetching Bookmarks by userId: {}, page: {}, size: {}", userIdentity.getUserId(), page, size);
        return bookmarkRepository.findByUserId(userIdentity, page, size);
    }

    @Override
    public List<Bookmark> getByUserIdAndTargetType(UserIdentity userIdentity, TargetType targetType, int page, int size) {
        log.debug("Fetching Bookmarks by userId: {}, targetType: {}, page: {}, size: {}",
                userIdentity.getUserId(), targetType, page, size);
        return bookmarkRepository.findByUserIdAndTargetType(userIdentity, targetType, page, size);
    }

    @Override
    public long countByUserId(UserIdentity userIdentity) {
        log.debug("Counting Bookmarks by userId: {}", userIdentity.getUserId());
        return bookmarkRepository.countByUserId(userIdentity);
    }

    @Override
    public Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(UserIdentity userIdentity, TargetType targetType, Long targetId) {
        log.debug("Fetching Bookmark by userId: {}, targetType: {}, targetId: {}",
                userIdentity.getUserId(), targetType, targetId);
        return bookmarkRepository.findByUserIdAndTargetTypeAndTargetId(userIdentity, targetType, targetId);
    }
}
