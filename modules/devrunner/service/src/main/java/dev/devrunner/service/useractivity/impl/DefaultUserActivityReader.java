package dev.devrunner.service.useractivity.impl;

// import dev.devrunner.exception.useractivity.UserActivityNotFoundException;
import dev.devrunner.model.useractivity.UserActivity;
import dev.devrunner.model.useractivity.UserActivityIdentity;
import dev.devrunner.service.useractivity.UserActivityReader;
import dev.devrunner.infra.useractivity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserActivity 도메인 조회 서비스 구현체
 *
 * CQRS 패턴의 Query 책임을 구현하며,
 * Infrastructure Repository를 활용한 조회 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultUserActivityReader implements UserActivityReader {

    private final UserActivityRepository userActivityRepository;

    @Override
    public UserActivity getById(UserActivityIdentity identity) {
        log.debug("Fetching UserActivity by id: {}", identity.getUserActivityId());
        return userActivityRepository.findById(identity)
                .orElseThrow(() -> new RuntimeException("UserActivity not found: " + identity));
                // .orElseThrow(() -> new UserActivityNotFoundException(identity));
    }

    @Override
    public List<UserActivity> getAll() {
        log.debug("Fetching all UserActivities");
        return userActivityRepository.findAll();
    }

    @Override
    public List<UserActivity> getByUserId(Long userId) {
        log.debug("Fetching UserActivities by userId: {}", userId);
        return userActivityRepository.findByUserId(userId);
    }
}
