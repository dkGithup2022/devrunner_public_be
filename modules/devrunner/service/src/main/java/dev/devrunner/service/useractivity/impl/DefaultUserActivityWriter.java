package dev.devrunner.service.useractivity.impl;

import dev.devrunner.model.useractivity.UserActivity;
import dev.devrunner.model.useractivity.UserActivityIdentity;
import dev.devrunner.service.useractivity.UserActivityWriter;
import dev.devrunner.infra.useractivity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * UserActivity 도메인 변경 서비스 구현체
 *
 * CQRS 패턴의 Command 책임을 구현하며,
 * Infrastructure Repository를 활용한 변경 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultUserActivityWriter implements UserActivityWriter {

    private final UserActivityRepository userActivityRepository;

    @Override
    public UserActivity upsert(UserActivity userActivity) {
        log.info("Upserting UserActivity: {}", userActivity.getUserActivityId());
        UserActivity saved = userActivityRepository.save(userActivity);
        log.info("UserActivity upserted successfully: {}", saved.getUserActivityId());
        return saved;
    }

    @Override
    public void delete(UserActivityIdentity identity) {
        log.info("Deleting UserActivity by id: {}", identity.getUserActivityId());
        userActivityRepository.deleteById(identity);
        log.info("UserActivity deleted successfully: {}", identity.getUserActivityId());
    }
}
