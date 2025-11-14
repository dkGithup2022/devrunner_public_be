package dev.devrunner.jdbc.user.repository;

import dev.devrunner.model.common.NotificationSettings;
import dev.devrunner.model.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserJdbcRepository 테스트
 *
 * @DataJdbcTest를 사용한 Spring Data JDBC 통합 테스트
 * Entity ↔ Domain 변환 로직 및 Derived Query 메서드 검증
 * EmailEncryptor를 ComponentScan에 포함하여 암호화/복호화 테스트
 */
@DataJdbcTest
@ComponentScan(basePackages = {
        "dev.devrunner.jdbc.user.repository",
        "dev.devrunner.encryption"  // EmailEncryptor 포함
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserJdbcRepositoryTest {

    @Autowired
    private UserJdbcRepository userRepository;

    // 테스트 데이터
    private final User sampleUser = new User(
            null,                           // userId (자동 생성)
            "google123",                    // googleId
            "test@example.com",             // email
            "testNickname",                 // nickname
            UserRole.USER,                  // userRole
            List.of("Company A"),           // interestedCompanies
            List.of("Seoul"),               // interestedLocations
            NotificationSettings.defaultSettings(), // notificationSettings
            UserMetrics.initial(),          // metrics
            Instant.now(),                  // lastLoginAt
            true,                           // isActive
            false,                          // isWithdrawn
            null,                           // withdrawnAt
            Instant.now(),                  // createdAt
            Instant.now()                   // updatedAt
    );

    private final UserIdentity testIdentity = new UserIdentity(1L);
    private final UserIdentity nonExistingIdentity = new UserIdentity(999L);

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        User userToSave = sampleUser;

        // when
        User saved = userRepository.save(userToSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getGoogleId()).isEqualTo(userToSave.getGoogleId());
        assertThat(saved.getEmail()).isEqualTo(userToSave.getEmail()); // 암호화/복호화 검증
        assertThat(saved.getNickname()).isEqualTo(userToSave.getNickname());
        assertThat(saved.getInterestedCompanies()).containsExactly("Company A");
        assertThat(saved.getInterestedLocations()).containsExactly("Seoul");
    }

    @Test
    void save_withNullId_generatesIdAndReturns() {
        // given
        User userWithNullId = sampleUser;

        // when
        User saved = userRepository.save(userWithNullId);

        // then
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(userWithNullId.getEmail());
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        User saved = userRepository.save(sampleUser);
        UserIdentity identity = new UserIdentity(saved.getUserId());

        // when
        Optional<User> found = userRepository.findById(identity);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getGoogleId()).isEqualTo(saved.getGoogleId());
        assertThat(found.get().getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // when
        Optional<User> found = userRepository.findById(nonExistingIdentity);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_withData_returnsConvertedList() {
        // given
        User saved1 = userRepository.save(sampleUser);
        User saved2 = userRepository.save(new User(
                null, "google456", "another@example.com", "anotherNickname", UserRole.USER,
                List.of(), List.of(), NotificationSettings.defaultSettings(), UserMetrics.initial(),
                Instant.now(), true, false, null, Instant.now(), Instant.now()
        ));

        // when
        List<User> all = userRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(User::getUserId)
                .containsExactlyInAnyOrder(saved1.getUserId(), saved2.getUserId());
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        // when
        List<User> all = userRepository.findAll();

        // then
        assertThat(all).isEmpty();
    }

    // ========== Derived Query 테스트 ==========

    @Test
    void findByGoogleId_existingGoogleId_returnsConvertedDomain() {
        // given
        User saved = userRepository.save(sampleUser);

        // when
        Optional<User> found = userRepository.findByGoogleId("google123");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getGoogleId()).isEqualTo(saved.getGoogleId());
    }

    @Test
    void findByGoogleId_nonExistingGoogleId_returnsEmpty() {
        // when
        Optional<User> found = userRepository.findByGoogleId("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_existingEmail_returnsConvertedDomain() {
        // given
        User saved = userRepository.save(sampleUser);

        // when - 평문 이메일로 검색 (내부에서 암호화)
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    void findByEmail_nonExistingEmail_returnsEmpty() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByNickname_existingNickname_returnsConvertedDomain() {
        // given
        User saved = userRepository.save(sampleUser);

        // when
        Optional<User> found = userRepository.findByNickname("testNickname");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getNickname()).isEqualTo(saved.getNickname());
    }

    @Test
    void findByNickname_nonExistingNickname_returnsEmpty() {
        // when
        Optional<User> found = userRepository.findByNickname("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByIdIn_existingIds_returnsConvertedList() {
        // given
        User saved1 = userRepository.save(sampleUser);
        User saved2 = userRepository.save(new User(
                null, "google456", "another@example.com", "anotherNickname", UserRole.USER,
                List.of(), List.of(), NotificationSettings.defaultSettings(), UserMetrics.initial(),
                Instant.now(), true, false, null, Instant.now(), Instant.now()
        ));
        User saved3 = userRepository.save(new User(
                null, "google789", "third@example.com", "thirdNickname", UserRole.USER,
                List.of(), List.of(), NotificationSettings.defaultSettings(), UserMetrics.initial(),
                Instant.now(), true, false, null, Instant.now(), Instant.now()
        ));

        // when
        List<User> found = userRepository.findAllByIdIn(List.of(
                new UserIdentity(saved1.getUserId()),
                new UserIdentity(saved3.getUserId())
        ));

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(User::getUserId)
                .containsExactlyInAnyOrder(saved1.getUserId(), saved3.getUserId());
    }

    @Test
    void findAllByIdIn_nonExistingIds_returnsEmptyList() {
        // when
        List<User> found = userRepository.findAllByIdIn(List.of(
                new UserIdentity(999L),
                new UserIdentity(998L)
        ));

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void existsById_existingId_returnsTrue() {
        // given
        User saved = userRepository.save(sampleUser);
        UserIdentity identity = new UserIdentity(saved.getUserId());

        // when
        boolean exists = userRepository.existsById(identity);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        // when
        boolean exists = userRepository.existsById(nonExistingIdentity);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_existingId_deletesSuccessfully() {
        // given
        User saved = userRepository.save(sampleUser);
        UserIdentity identity = new UserIdentity(saved.getUserId());

        // when
        userRepository.deleteById(identity);

        // then
        assertThat(userRepository.existsById(identity)).isFalse();
    }
}
