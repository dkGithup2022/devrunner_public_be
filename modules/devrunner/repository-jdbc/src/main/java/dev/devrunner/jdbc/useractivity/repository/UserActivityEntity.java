package dev.devrunner.jdbc.useractivity.repository;

import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.useractivity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * UserActivity Spring Data JDBC Entity
 *
 * Model 클래스 스펙을 기반으로 생성된 데이터베이스 매핑용 엔티티
 */
@Table("user_activities")
@Getter
@AllArgsConstructor
public class UserActivityEntity {
    @Id
    private Long id;
    private Long userId;
    private ActivityType activityType;
    private TargetType targetType;
    private Long targetId;
    private String targetTitle;
    private String targetCompany;
    private Instant createdAt;
    private Instant updatedAt;
}
