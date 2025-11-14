package dev.devrunner.service.communitypost;

import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.communitypost.CommunityPostRead;

import java.util.List;

/**
 * CommunityPost 도메인 조회 서비스 인터페이스
 *
 * CQRS 패턴의 Query 책임을 담당하며,
 * Infrastructure Repository 기반으로 조회 로직을 제공합니다.
 */
public interface CommunityPostReader {

    /**
     * CommunityPost 읽기 (조회수 증가 포함)
     *
     * @param identity CommunityPost 식별자
     * @return CommunityPostRead 엔티티
     */
    CommunityPostRead read(CommunityPostIdentity identity);

    /**
     * ID로 CommunityPost 조회 (조회수 증가 없음)
     *
     * @param identity CommunityPost 식별자
     * @return CommunityPostRead 엔티티
     */
    CommunityPostRead getById(CommunityPostIdentity identity);

    /**
     * 모든 CommunityPost 조회
     *
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> getAll();

    /**
     * 사용자 ID로 CommunityPost 조회
     *
     * @param userId 사용자 ID
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> getByUserId(Long userId);

    /**
     * 회사명으로 CommunityPost 조회
     *
     * @param company 회사명
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> getByCompany(String company);

    /**
     * 지역으로 CommunityPost 조회
     *
     * @param location 지역
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> getByLocation(String location);

    /**
     * 여러 ID로 CommunityPost 조회 (bulk fetch, N+1 방지)
     *
     * @param identities CommunityPost 식별자 목록
     * @return CommunityPostRead 목록
     */
    List<CommunityPostRead> getByIds(List<CommunityPostIdentity> identities);
}
