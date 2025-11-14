package dev.devrunner.service.bookmark.impl;

import dev.devrunner.infra.bookmark.repository.BookmarkRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.exception.bookmark.BookmarkNotFoundException;
import dev.devrunner.service.bookmark.BookmarkReader;
import dev.devrunner.service.bookmark.BookmarkWriter;
import dev.devrunner.exception.bookmark.DuplicateBookmarkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Bookmark 서비스 구현체
 *
 * 북마크 추가/삭제를 처리하는 비즈니스 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultBookmarkWriter implements BookmarkWriter {

    private final BookmarkReader bookmarkReader;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addBookmark(UserIdentity userIdentity, TargetType targetType, Long targetId) {
        log.info("User {} adding bookmark to {} {}", userIdentity.getUserId(), targetType, targetId);

        // 1. 이미 북마크되어 있는지 확인
        Optional<Bookmark> existing = bookmarkReader.findByUserIdAndTargetTypeAndTargetId(userIdentity, targetType, targetId);

        if (existing.isPresent()) {
            log.warn("User {} already bookmarked {} {}", userIdentity.getUserId(), targetType, targetId);
            throw new DuplicateBookmarkException(
                    String.format("Bookmark already exists for user %d on %s %d",
                            userIdentity.getUserId(), targetType, targetId));
        }

        // 2. 새로운 북마크 생성
        Bookmark newBookmark = Bookmark.create(userIdentity.getUserId(), targetType, targetId);
        bookmarkRepository.save(newBookmark);
        log.info("User {} created new bookmark on {} {}", userIdentity.getUserId(), targetType, targetId);

        // 3. 북마크 추가한 사용자의 bookmarkCount 증가
        incrementUserBookmarkCount(userIdentity);
    }

    @Override
    @Transactional
    public void removeBookmark(UserIdentity userIdentity, TargetType targetType, Long targetId) {
        log.info("User {} removing bookmark from {} {}", userIdentity.getUserId(), targetType, targetId);

        // 1. 북마크 찾기
        Bookmark bookmark = bookmarkReader.findByUserIdAndTargetTypeAndTargetId(userIdentity, targetType, targetId)
                .orElseThrow(() -> {
                    log.warn("No bookmark found for user {} on {} {}", userIdentity.getUserId(), targetType, targetId);
                    return new BookmarkNotFoundException(
                            String.format("Bookmark not found for user %d on %s %d",
                                    userIdentity.getUserId(), targetType, targetId));
                });

        // 2. 삭제
        bookmarkRepository.deleteById(new BookmarkIdentity(bookmark.getBookmarkId()));
        log.info("User {} removed bookmark from {} {}", userIdentity.getUserId(), targetType, targetId);

        // 3. 북마크 삭제한 사용자의 bookmarkCount 감소
        decrementUserBookmarkCount(userIdentity);
    }

    /**
     * User의 bookmarkCount 증가
     */
    private void incrementUserBookmarkCount(UserIdentity userIdentity) {
        User user = userRepository.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found: " + userIdentity.getUserId()));

        User updatedUser = user.incrementBookmarkCount();
        userRepository.save(updatedUser);
        log.info("User {} bookmarkCount incremented", userIdentity.getUserId());
    }

    /**
     * User의 bookmarkCount 감소
     */
    private void decrementUserBookmarkCount(UserIdentity userIdentity) {
        User user = userRepository.findById(userIdentity)
                .orElseThrow(() -> new RuntimeException("User not found: " + userIdentity.getUserId()));

        User updatedUser = user.decrementBookmarkCount();
        userRepository.save(updatedUser);
        log.info("User {} bookmarkCount decremented", userIdentity.getUserId());
    }
}
