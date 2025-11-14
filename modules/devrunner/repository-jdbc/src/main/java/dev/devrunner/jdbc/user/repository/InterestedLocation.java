package dev.devrunner.jdbc.user.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * User의 관심 지역 정보를 저장하는 자식 엔티티
 * Spring Data JDBC가 자동으로 user_interested_locations 테이블과 매핑
 *
 * @Table 어노테이션 불필요 - Spring Data JDBC가 부모 테이블명 + 필드명으로 자동 생성
 * 예: UserEntity의 interestedLocations -> user_interested_locations 테이블
 */
@Table("user_interested_locations")
@Getter
@AllArgsConstructor
public class InterestedLocation {
    @Column("location_name")
    private String locationName;
}
