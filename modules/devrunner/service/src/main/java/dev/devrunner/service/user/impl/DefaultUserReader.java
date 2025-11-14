package dev.devrunner.service.user.impl;

import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.user.UserReader;
import dev.devrunner.infra.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * User 도메인 조회 서비스 구현체
 * <p>
 * CQRS 패턴의 Query 책임을 구현하며,
 * Infrastructure Repository를 활용한 조회 로직을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultUserReader implements UserReader {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(UserIdentity identity) {
        log.debug("Finding User by id: {}", identity.getUserId());
        return userRepository.findById(identity);
    }

    @Override
    public List<User> findAll() {
        log.debug("Finding all Users");
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        log.debug("Finding User by googleId: {}", googleId);
        return userRepository.findByGoogleId(googleId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding User by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        log.debug("Finding User by nickname: {}", nickname);
        return userRepository.findByNickname(nickname);
    }
}
